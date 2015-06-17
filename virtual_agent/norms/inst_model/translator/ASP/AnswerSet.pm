package ASP::AnswerSet;
use Data::Dumper;
use strict;
use ASP::Parser;

my $DEBUG = 1;

our @INTERNALS = qw/instant ev ifact before next final init happened/;

our @TIMED = qw/occured observed holdsat/;

our %tagclass = ( 
    'TIMED'=> sub { my $t = $_[0]; 
		    foreach(@TIMED){ 
			return 1 if ($_ eq $t->name)};
		    return 0;},
    'ISTATE'=> sub { $_[0]->name eq 'holdsat'},
    'FINAL'=> sub { $_[0]->name eq 'final'},
    'OCCURED'=> sub { $_[0]->name eq 'occured'},
    'OBSERVED'=> sub{ $_[0]->name eq 'observed'},
    'VIOL'=> sub { $_[0]->name eq 'occured' &&
		       ref $_[0]->arg(0) eq 'ASP::Atom' && 
		       $_[0]->arg(0)->name eq 'viol';
	       },
    
    'INTERNAL'=> sub{my $t = $_[0];
		     foreach my $i (@INTERNALS){
			 return 1 if  $i eq $t->name;
		     }
		     return 0;
		 }
);


sub new{
    my $class = shift;
    my $atoms = shift;
    my $atompool = shift;
#    print STDERR " Got ",@{$atoms}} ," atoms\n" if $DEBUG;
    my $self =  bless {-atoms=>$atoms,-atompool=>$atompool, -instants=>[]} ,$class;
    foreach my $a (@{$atoms}){
	$self->classify_atom($a);
    }
    
    return $self;
}

sub allatoms{
    my $self = shift;
    return values(%{$self->{-atompool}});
}

sub atoms{
    my $self= shift;
    return @{$self->{-atoms}};
}

sub tostring{
    my $self = shift;
    print "Stable Model: \n";
    foreach my $atom (sort {$a->cmp($b)} ($self->atoms)){
#	print "Atom is ",Dumper($atom),"\n";
	print $atom->tostring, "\t\t[",join(",",$atom->tags),"]\n";
    }
}

sub occured_atoms{
    my $self = shift;
    my $inst = shift;

    return ASP::AnswerSet::filterTag(['OCCURED'],
				     ASP::AnswerSet::filterTime($inst,$self->atoms));
}


sub env_atoms{
    my $self = shift;
    my $inst = shift;
    
    
    my @obs =  ASP::AnswerSet::filterTag(['OBSERVED'],
					 ASP::AnswerSet::filterTime($inst,$self->atoms));
    
    my @envs = ASP::AnswerSet::filter( sub {
	$_[0]->name eq 'evtype' && $_[0]->arg(1)->name eq 'env';
    },$self->atoms);
    
    
    my @envnames = map {$_->arg(0)->tostring}(@envs);
    print "*** Got ", join (",",@envnames),"\n";
    my @allenvs;


    foreach my  $t (@envnames){
	foreach my $e (@obs){
	    if($e->arg(0)->tostring eq $t){
		push @allenvs,$e;
		print "**** GOT ",$e->tostring,"\n";
	    }
	}
    }
    return @allenvs;
    
}

sub observed_atoms{
    my $self = shift;
    my $inst = shift;

    return ASP::AnswerSet::filterTag(['OBSERVED'],
				     ASP::AnswerSet::filterTime($inst,$self->atoms));
}

sub istate_atoms{
    my $self = shift;
    my $inst = shift;

    return ASP::AnswerSet::filterTag(['ISTATE'],
				     ASP::AnswerSet::filterTime($inst,$self->atoms));
}
sub instants{
    my $self = shift;
    
    return grep { ($_ cmp $self->{-final}) <=0} (sort {$a cmp $b} (@{$self->{-instants}}));
}

sub num_instants{
    my $self = shift;
    my $n = @{$self->{-instants}};
    return $n;
}

sub classify_atom{
    my $self = shift;
    my $atom = shift;
    if ($atom->name eq 'instant'){
	push @{$self->{-instants}}, $atom->arg(0)->name;
    }
    
    if($atom->name eq 'final'){
	my $inst = $atom->arg(0)->name;

	if($self->{-final}){
	    if(($inst cmp $self->{-final}) < 0){
		$self->{-final} = $inst;
	    }
	}else{
	    $self->{-final} = $atom->arg(0)->name;
	}
    }
    foreach my $k (keys %tagclass){
	my $test = $tagclass{$k};
	if (&$test($atom)){
	    $atom->add_tag($k);
	}
    }
    return $atom;
}




#all of these are global methods, take a an array of atoms and filter them 
sub filter {
    my $fsub = shift;

    my @rset ;
    while(my $atom = shift){
#	print "Applying filter $fsub to $atom\n";
	if(&$fsub($atom)){
#	    print "MATCH\n";
	    push @rset,$atom;
	}
    }
    return @rset;
}

sub filterTime{
    my $time = shift; 
#    print "Filtering on time $time\n";
    return filter sub {
	my $atom = shift;
	$atom->match_tag('TIMED') && 
	    $atom->arg(1)->name eq $time },@_;
}

sub filterName{
    my $name = shift;
 #   print "filtering on \"$name\"\n";
    
    return filter sub{
	my $atom = shift;
#	print "checking $name == ",$atom->tostring,"\n";
	return $atom->name eq $name;
    },@_;
}

sub filterTag{
    my $tags = shift;
#    print "filtering on ", join (",",@{$tags}),"\n";
    return filter sub  { 
	my $atom = shift;
	foreach my $t (@{$tags}){
	    return 1 if $atom->match_tag($t);
	    return 0;}},@_;
} 
1;
