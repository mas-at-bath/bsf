package InstAL::VarMapping;

use Carp qw/cluck/;
use Data::Dumper;
# a set of var=>type mappings;

# foreach var we want to iterate through each possible value for each type. 
use vars qw/$DEBUG $AUTOLOAD/;

$DEBUG= $ENV{IALDBG};

sub new{
    my $class = shift;
    my $varrefs = shift; # an array
    my $onlyused = shift;
    
    my $numvars = @{$varrefs};
    my @setsizes = ();
    my @vec =();
    my @all_lits;
    my $hasmore = 0;
    print "there are $numvars vars\n" if $DEBUG;
    $hasmore = 1;

    foreach(@{$varrefs}){
	my $literals;
	if($onlyused){
	    $literals = $_->type->getAllUsedLiterals;
	}else{
	    $literals= $_->type->getAllLiterals;
	}
	my $i = @{$literals};
	
	die "Can't ground an unbound variable " if $_->type->name eq 'ANY';
	print $_->var ," (",$_->type->name,") has $i literals:",join(",",map{$_->name}(@{$_->type->literals})),"\n" if $DEBUG;

	if($i==0){
	    
	    $_->warn("Type ". $_->type->name." is referenced  but has no literals, can't ground\n");
	    $hasmore = 0;
	}
	push @setsizes,$i;
	push @all_lits,$literals;
	push @vec,0;
	
    }
    
    
    return bless {-varrefs=>$varrefs,
		  -nv=>$numvars,
		  -setsizes=>\@setsizes,
		  -vec=>\@vec,
		  -literals=>\@all_lits,
		  -hasmore=>$hasmore},$class;
    
    
}

sub toString{
    my $self = shift;
    my @mappings;
    if(!$self->hasMore){
        return "No more mappings";

    }
    for(my $i = 0; $i < $self->{-nv};$i++){
	my $vname = $self->{-varrefs}->[$i]->var;
	my $index = $self->{-vec}->[$i];
        push @mappings, $vname.":".$self->{-literals}->[$i]->[$index]->name;
       
    }
    return '['.join(",",@mappings)."]";
}

sub getVar{
    my $self = shift;
    my $var = shift;
    
    for(my $i = 0; $i < $self->{-nv};$i++){
	if($self->{-varrefs}->[$i]->var eq $var){
	    my $index = $self->{-vec}->[$i];
	    
	    print "Found var at $i looking for literal $index\n" if $DEBUG;
	    print "looking at $i $index of $var in @{$self->{-literals}}\n" if $DEBUG;
	    #print Dumper($self);
	    return   $self->{-literals}->[$i]->[$index]->name;
	}
    }
    die ("Couldn't resolve var $var\n");
}



sub incrVect{
    my $self = shift;
    my $i = shift;
    my $d = shift;
    my $v = shift;
    my $s = shift;

 #   print "v :", join(",",@{$v}),"\n";
  #  print "ss:",join(",",@{$s}),"\n";

    if($i==$d){
#	print "a;\n";
	return 0;

    }
    
    if($v->[$i]<($s->[$i]-1)){
	$v->[$i]++;
#	print "b;\n";
	return 1;

    }else{ #overflow of digit I;
	if($i < $d-1){
	    $v->[$i] = 0;
	    return $self->incrVect($i+1,$d,$v,$s);
	}else{
#	    print "c;\n";
	    return 0; # last digit
	}
    }
    return 1;
}

sub nextMapping{
    my $self = shift;
    my $v = $self->{-vec};
    my $nv = $self->{-nv};
    my $ss =$self->{-setsizes};
    
    if(!$self->incrVect(0,$nv,$v,$ss)){
	$self->{-hasmore} = 0;
#	print "Last mapping\n";
	return 0;
    }
    return 1;
    
}


sub hasMore{
    my $self = shift;
    return $self->{-hasmore};
}
1;
