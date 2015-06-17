package InstAL::Ref;
use InstAL::CodeFragment;
use base InstAL::CodeFragment;
use OCC::Resolver;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub toString{
    die "Badness";
}


sub resolveVars{
    my $self = shift;
    return $self;

}
sub genASP{
    die "Badness";

}


sub resolveLiteral{
    my $self = shift;
    my $mapping = shift;
    my $name = $self->innerASP($mapping)->tostring;
    my $lp = InstAL::LiteralPool->lp;
    my $l;
    if(!($l =$lp->get_literal($name,1))){
	$l = $lp->add_literal($name,$self->getType());
    }
    return $l
}

1;


