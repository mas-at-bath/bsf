package ASP::Atom;
use strict;
sub new {
    my $class = shift; 
    my $name = shift; 
    my $args = shift;
    
    $args = [] if !$args;
    return bless { -name=>$name,
		   -args=>$args,
		   -tags=>[]
    },$class;
}

sub args{
    my $self = shift;
    return undef if !$self->{-args};
    return @{$self->{-args}};
}

sub add_tag{
    my $self= shift;
    my $tag = shift;
    push @{$self->{-tags}}, $tag;
}

sub match_tag($$){
    my $self = shift; 
    my $tag= shift;
    foreach my $t ( @{$self->{-tags}}){
	return 1 if $t eq $tag;
    }
    return 0;
    
}

sub tags{
    my $self = shift; 
    return @{$self->{-tags}};
}

sub name{
    my $self = shift;
    return $self->{-name};
}   


sub tostring { 
    my $self = shift;
    my $base = "";
    
    if($self->match_tag('NOT')){
	$base = "not ";
    }
    if(@{$self->{-args}}){
	return $base.$self->{-name}."(".join(",",map {$_->tostring} (@{$self->{-args}})).")";
	
    }else{
	return $base.$self->{-name};
    }
}

sub arg{
    my $self = shift;
    my $an = shift;
    return @{$self->{-args}}[$an];
}

sub numargs{
    my $self = shift;
    return 0 if !$self->{-args};
    my $i = @{$self->{-args}};
    return $i;
}

sub docmp{
    my ($arg1,$arg2) = @_;
    

    my $rv = $arg1->name cmp $arg2->name;
    return $rv if $rv !=0;
    
    if($arg1->numargs > $arg2->numargs){
	return 1;
    }elsif($arg1->numargs < $arg1->numargs){
	return -1;
    }elsif($arg1->numargs==0){
	return 0;
    }else{
	for(my $i = 0;$i < $arg1->numargs;$i++){
	    my $rv=ASP::Atom::docmp($arg1->arg($i),
				    $arg2->arg($i));
	    if($rv!=0){
		return $rv;
	    }
	}
    }
    return 0;
    

}


sub cmp{
    my $self = shift;
    my $other = shift;
    return ASP::Atom::docmp($self,$other);
}

sub sort{
    my $class = shift;
    return sort {$a->cmp($b)} (@_);
}

1;
