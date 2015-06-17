package InstAL::Expr::EvTypeExpr;

use InstAL::Expr;
use OCC::Resolver;
use ASP::Atom;
use strict;
use vars qw /@ISA $AUTOLOAD/;
use Data::Dumper;
use base 'InstAL::Expr::FluentExpr';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}
#FIXME: is this finished?
sub new{
    my $type = shift;
    my $lhs = shift;
    my $fluentref = shift;
    
    return bless {-fluentref=>$fluentref},$type;
}

sub toString{
    my $self = shift;
    return $self->fluentref->toString;
}

sub resolveVars{
    my $self = shift;
    $self->fluentref->resolveVars(@_);
}

sub genASP{
    my $self = shift;
    my $mapping = shift;
    
    return [$self->fluentref->genASP($mapping)] if $self->fluentref->fluent->is_static;
    return [ASP::Atom->new('holdsat',[$self->fluentref->genASP($mapping),ASP::Atom->new('I')])];
}
