package InstAL::Expr::NotExpr;
use InstAL::Expr;
use OCC::Resolver;
use InstAL::LiteralPool;
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

sub is_static{
    my $self =  shift;
    return $self->fluentref->is_static;
}
sub toString{
    my $self = shift;
    return "not ". $self->fluentref->toString;
}


sub resolveVars{
    my $self = shift;
    $self->fluentref->resolveVars(@_,InstAL::LiteralPool->fluentType());
}

sub genASP{
    my $self = shift;
    my $mapping = shift;

    my $cld = $self->fluentref->genASP($mapping);
    $cld->[0]->add_tag('NOT');
    return $cld;
    
}
