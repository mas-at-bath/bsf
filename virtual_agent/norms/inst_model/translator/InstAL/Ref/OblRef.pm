package InstAL::Ref::OblRef;
use InstAL::Expr;
use OCC::Resolver;
use strict;
use vars qw /@ISA $AUTOLOAD/;

use base 'InstAL::Ref::FluentRef';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub is_static{
    my $self = shift;
    return 0;
}
 
sub new{
    my $type = shift;
    my $obl = shift; # eventref
    my $dl = shift; # eventref
    my $viol = shift; # eventref
    
    return bless {-obl=>$obl,
		  -dl=>$dl,
		  -viol=>$viol
    },$type;
}



sub getType{
    my $self = shift; 
    my $lp = InstAL::LiteralPool->lp;
    return $lp->typeObl;
}

sub resolveVars{
    my $self = shift;
    $self->obl->resolveVars(@_);
    $self->dl->resolveVars(@_);
    $self->viol->resolveVars(@_);
}


sub innerASP{
    my $self = shift;
    my $mapping = shift;
    my $obl = $self->obl->innerASP($mapping);
    my $dl = $self->dl->innerASP($mapping);
    my $viol = $self->viol->innerASP($mapping);
    return ASP::Atom->new('obl',[$obl,$dl,$viol]);

}


sub fluent{
	return InstAL::LiteralPool->lp()->get_fluents('obl');
	
}

sub toString{
    my $self = shift;
    return "obl(".
	$self->obl->toString.",".
	$self->dl->toString.",".
	$self->viol->toString.")";
    
}
1;
