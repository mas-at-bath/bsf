package InstAL::Expr::FluentExpr;
use InstAL::Expr;
use OCC::Resolver;
use ASP::Atom;
use vars qw /@ISA/;

@ISA = qw/InstAL::Expr/;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new{
    my $type = shift;
    my $fluentref = shift;    
    return bless {-fluentref=>$fluentref},$type;
}

sub toString{
    my $self = shift;
    return $self->fluentref->toString;
}

sub is_static{
    my $self = shift;
    return $self->fluentref->is_static;
}
sub resolveVars{
    my $self = shift;
    $self->fluentref->resolveVars(@_);
}

sub genASP{
    my $self = shift;
    my $mapping = shift;

    return [$self->fluentref->innerASP($mapping)] if $self->fluentref->is_static;
    return [ASP::Atom->new('holdsat',[$self->fluentref->innerASP($mapping),ASP::Atom->new('I')])];
}

1;
