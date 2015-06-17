package  InstAL::Expr::VarExpr;

sub resolveVars {
	my $self = shift;
	my $rule = shift;
	$self->{-lhs} = $rule->add_varref( $self->lhs, $self );
	$self->{-rhs} = $rule->add_varref( $self->rhs, $self );

}

sub genASP {

}
