package InstAL::Expr::EqExpr;

use InstAL::Expr;
use OCC::Resolver;
use ASP::Atom;
use strict;
use vars qw /@ISA $AUTOLOAD/;
use base 'InstAL::Expr';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}
use base 'InstAL::Expr::BinaryExpr';

sub new{
    my $type = shift;
    my $lhs = shift;
    my $rhs = shift;
    
    return bless {-lhs=>$lhs,
		  -rhs=>$rhs},$type;
}

sub toString{
    my $self = shift;   
    return $self->lhs->toString ."=".$self->rhs->toString;
}

sub genASP{ 
	return new ASP::Atom('eq',[$_[0]->lhs()->genASP(@_), $_[0]->rhs()->genASP(@_)]);
}


sub test{
    my $self = shift;
    my $lhs = shift;
    my $rhs = shift;
    return $lhs eq $rhs;
}




1;