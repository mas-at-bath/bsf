package InstAL::Expr::AndExpr;
use base InstAL::Expr;
use OCC::Resolver;
use base 'InstAL::Expr::BinaryExpr';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub new{
    my $type = shift;
    my $subs = shift;
    
    return bless {-subs=>$subs},$type;
}

sub is_static{
    my $self =shift;

    foreach(@{$self->subs}){
	if(!$_->is_static){
	    return 0;
	}
    }
    
    return 1;


}
sub validMapping{
    my $self  = shift;
    my $mapping = shift;
    foreach(@{$self->subs}){
	my ($valid,$rule) = $_->validMapping($mapping);
	if(!$valid){
	    return ($valid,$rule);
	}
    }    
    return 1;
    
}
sub resolveVars{
    my $self = shift;
    foreach(@{$self->subs}){
	$_->resolveVars(@_);
    }
}
sub toString{
    my $self = shift;
    return join (",",map {$_->toString} (@{$self->subs}));
}

sub genASP{
    my $self = shift;
    my $mapping = shift;

    my @children =();
    foreach(@{$self->subs}){
	push @children,@{$_->genASP($mapping)};
    }
    return \@children;
}

1;
