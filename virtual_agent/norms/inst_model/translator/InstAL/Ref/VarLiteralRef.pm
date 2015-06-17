package InstAL::Ref::VarLiteralRef;
use InstAL::Ref;
use base 'InstAL::Ref';
use OCC::Resolver;
use ASP::Atom;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub resolveVars{
    my $self = shift;
    my $rule = shift;

    my $nargs =  @{$self->args};
    for(my $i = 0; $i < $nargs;$i++){
	
	$self->args->[$i] = $rule->add_varref($self->args->[$i],$self) ;
	my $newref =  $rule->add_varref($self->get_args($i),$self) ;
	die if !$newref;
	$self->args->[$i] =$newref;
    }
    return $self;
}
sub new{
    my $class = shift;
    my $name = shift;
    my $args = shift;
    my $type = shift;
    
    my $self = {
	-name=>$name,
	-args=>$args,
	-type=>$type,
    };
    
    return bless $self,$class;
}

sub toString{
    my $self = shift;
    return $self->name.'('.join(',',map {$_->toString}(@{$self->args})).')';
}


sub innerASP{
    my $self = shift;
    my $mapping = shift;
    my @aspargs;
    foreach (@{$self->args}){
	push @aspargs, $_->innerASP($mapping,1);
    }
    return ASP::Atom->new($self->name,\@aspargs);

}
1;
