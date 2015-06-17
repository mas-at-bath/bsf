package InstAL::Expr::BinaryExpr;

use InstAL::Expr;
use OCC::Resolver;
use ASP::Atom;
use strict;
use vars qw /@ISA $AUTOLOAD/;
use Data::Dumper;
@ISA = qw/InstAL::Expr/;


sub toString{
    my $self = shift;   
    return $self->lhs->toString ."!=".$self->rhs->toString;
}

sub resolveVars{
    my $self = shift;
    my $rule = shift;
    $self->set_lhs($self->lhs->resolveVars($rule,$self));
    $self->set_rhs($self->rhs->resolveVars($rule,$self));
}



sub validMapping{
    my $self = shift;
    my $mapping = shift;
    my ($lhsv,$rhsv);
    
    $lhsv = $self->lhs->innerASP($mapping)->tostring;
    $rhsv = $self->rhs->innerASP($mapping)->tostring;    
    return  ($self->test($lhsv,$rhsv),$self);
}

sub genASP{
    return [];
}
#eqs are not generated
sub innerASP{
    my $self = shift;
    die "Binary cannot be inner";

}

