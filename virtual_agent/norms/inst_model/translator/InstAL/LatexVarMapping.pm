package InstAL::LatexVarMapping;
use InstAL::Type;
use OCC::Resolver;

use strict;


use vars qw/$AUTOLOAD/;
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

use Carp qw/cluck/;
# a set of var=>type mappings;

# foreach var we want to iterate through each possible value for each type. 

use vars qw/$DEBUG $AUTOLOAD/;
use InstAL::VarMapping;

$DEBUG=$ENV{IALDBG};

use base 'InstAL::VarMapping';




sub new{
    my $class = shift;
    my $varrefs = shift; # an array
    
    my $names = {};
    my $numvars = @{$varrefs};
    my $typerefs = {};

    my $self= bless {
	-varrefs=>$varrefs,
	-typerefs=>$typerefs,
	-nv=>$numvars,
	-mapping=>$names
    },$class;
    
    foreach (@{$varrefs}){
	push @{$typerefs->{$_->type->name}},$_;
    }
    
    my %ct;
    foreach (@{$varrefs}){
	my $n = @{$typerefs->{$_->type->name}};
	$names->{$_->var} = $self->pickVariableName($_->type,$ct{$_->type},$n);
#	print STDERR "Picked name ",$names->{$_->var}," for var ",$_->var,"\n";
	$ct{$_->type}++;
    }
    print "there are $numvars vars\n" if $DEBUG;
    return $self;
}

sub quantHead{
    my $self =shift;
    
    return "" if !$self->{-nv};
    my $tv;
    my @typenames = sort {$a cmp $b}(keys (%{$self->typerefs}));
    my $str ;
    foreach (@typenames){
	$str.= join(",",map {$self->getVar($_->var)} (@{$self->typerefs->{$_}}));
	$str.=' \in \type_'.$_." ";
    }

    return $str;
}


sub pickVariableName{
    my $self = shift;
    my $type = shift;
    my $num =shift;
    my $total = shift ;
    my $i =lc(substr($type->name,0,1)); 
    while($num--){
	$i.="'";
    }
    return $i;
    
    
}


sub getVar{
    my $self = shift;
    my $var = shift;

    return $self->get_mapping($var);
}

1;
