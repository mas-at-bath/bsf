package OCC::Resolver;

# Quick hack to save having to write accesors, assume $self is a
# hashref and deals with the following type of accessors for property
# 'foo' In all cases the function should die if $self->{-foo} does not
# exist

# $self->foo : $self->{-foo}
# $self->is_foo : $self->{-foo}?1:0;
# $self->get_foo($x): $self->{-foo}== arrayref: $self->{-foo}->[$x]
# $self->get_foo($x): $self->{-foo}== hashref: $self->{-foo}->{$x}
# $self->add_foo($x): $self->{-foo} == arrayref: push  @{$self->{-foo}},$x; 
# $self->add_foo($x): $self->{-foo} == arrayref: push @{$self->{-foo}},$x;
# $self->set_foo($x,$y) $self->{-foo} == hashref :$self->{-foo}->{$x} = $y 
# $self->set_foo($x): $self->{-foo}  != hashref: $self->{-foo} = $x;


use Data::Dumper;
use Carp qw/cluck/;
use vars qw/$DEBUG/;
use strict;
#$DEBUG=1;
sub resolve{
    my $pack = shift;
    my $autoload = shift;
    my $self = shift;
  

    if(ref($self) eq $pack){
	my $fn = $autoload;
	$fn=~s/.*:://;	
	return if $fn eq 'DESTROY';
	my $arg = shift;
	my $arg2 = shift;
	
	print "calling $self->$fn($arg,$arg2)\n" if $DEBUG;
	my $rv;
#	print "$self -> \"$fn\"\n";
	if($fn =~/^set_(.*)$/&& exists($self->{'-'.$1})){
	    my $str = '-'.$1;
	    if(ref($self->{$str}) eq 'HASH' ){
		$self->{$str}->{$arg}= $arg2;
		print "$self -> {-$1}->{$arg} = $arg2\n" if $DEBUG;
	    }else{
		die "Invalid second argument to $self->$fn, type was ".ref($self->{$str})  if $arg2;
		
		print "$self -> {-$1} = $arg\n"if $DEBUG;
		$self->{$str} = $arg;
	    }	    
	}elsif($fn=~/^get_(.*)$/&& exists($self->{'-'.$1})){
	    my $str = '-'.$1;
	    if(ref($self->{$str}) eq 'ARRAY'){
		$rv = $self->{$str}->[$arg];
	    }elsif(ref($self->{$str}) eq 'HASH'){
		$rv = $self->{$str}->{$arg};
	    }else{
		$rv = $self->{$str};
		
	    }
	}elsif($fn=~/^add_(.*)$/ && exists($self->{'-'.$1})
	       &&ref($self->{'-'.$1}) eq 'ARRAY'){
	    my $str = '-'.$1;
	    print "ref ($str):",$self->{$str},",",ref($self->{$str}),"\n" if $DEBUG;
	    print "adding $arg to $1\n" if $DEBUG;
	    push @{$self->{'-'.$1}},$arg;
	}elsif($fn=~/^is_(.*)$/ && exists($self->{'-'.$1})){
	    $rv =  $self->{'-'.$1}?1:0;
	}elsif(exists($self->{'-'.$fn})){
	    my $str = '-'.$fn;
	    cluck "Unwantned argument to $autoload : $arg " if $arg || $arg2;
	    die "Unwantned argument to $autoload : $arg " if $arg || $arg2;
	    
	    print "self->{$str}: ",$self->{$str},"(", ref($self->{$str}),")\n" if $DEBUG;
	    $rv = $self->{$str};

	}else{
	    print "self was:",Dumper($self) if $DEBUG;
	    cluck "unknown accessor  $autoload\n";
	    die "unknown accessor  $autoload\n";
	}
	print "<--- $rv\n" if $DEBUG;
	return $rv;
    }
}

1;
