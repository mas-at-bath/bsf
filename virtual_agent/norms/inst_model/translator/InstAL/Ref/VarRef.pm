package InstAL::Ref::VarRef;

use OCC::Resolver;
use ASP::Atom;
use InstAL::Ref;

use base 'InstAL::Ref';

use Carp qw/cluck/;
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new{
    my $class = shift;
    my $var = shift;
    my $type = shift;

    cluck && die "type not string" if ref($var);
    my $self = {
	-var=>$var,
	-type=>$type
    };
    
    return bless $self,$class;
}


sub getType{
    my $self = shift;
    return $self->type;
}


sub resolveVars{
    my $self = shift;
    my $rule = shift;
    return $rule->add_varref($self,$rule);
}


sub resolveLiteral{
    my $self = shift;
    my $mapping = shift;
    my $name = $self->innerASP($mapping)->tostring;
    my $lp = InstAL::LiteralPool->lp;
    my $l;
    if(!($l =$lp->get_literal($name,1))){
	$l = $lp->add_literal($name,$self->type);
    }
    return $l
}

sub toString{
    my $self = shift;
    return $self->var;
}


    #Hack, we know that we should be getting an atom or even eventref from
#the literal pool...
sub innerASP{
    my $self = shift;
    my $mapping = shift;
    cluck if !$mapping;
    my $var = $self->var;
    my $name = $mapping->getVar($var);
    if($self->type->name eq 'ANY'){
	die "Variable $var unspecified";
    }
    return ASP::Atom->new($name);
}

sub genASP{
    my $self = shift;
    my $mapping = shift;
    cluck if !$mapping;
    my $var = $self->var;
    
    my $lp = InstAL::LiteralPool->lp;

    if ($self->type != $lp->typeEvent() &&
	$self->type!= $lp->typeFluent()){
	die "Invalid type for top-level generation";
    }
    
    #yet another evil hack, 
    my $inner = $self->innerASP($mapping); 
    if ($self->type->name eq 'Event'){
	return ASP::Atom->new('occured',[$inner,ASP::Atom->new('I')]);
    }else{
	return ASP::Atom->new('holdsat',[$inner,ASP::Atom->new('I')]);
    }
    return 
    
}

1;
