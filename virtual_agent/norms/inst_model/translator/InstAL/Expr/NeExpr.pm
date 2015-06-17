package InstAL::Expr::NeExpr;

use InstAL::Expr;
use OCC::Resolver;
use ASP::Atom;
use strict;
use vars qw /@ISA $AUTOLOAD/;
use Data::Dumper;
use InstAL::Expr::BinaryExpr;
use base 'InstAL::Expr::BinaryExpr';


sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new{
    my $type = shift;
    my $lhs = shift;
    my $rhs = shift;

    return bless {-lhs=>$lhs,
		  -rhs=>$rhs},$type;
}

sub toString{
    my $self = shift;   
    return $self->lhs->toString ."!=".$self->rhs->toString;
}

sub test{
    my $self = shift;
    my $lhs = shift;
    my $rhs = shift;
    return $lhs ne $rhs;
}
sub genASP{
		return [];
	
}



1;
