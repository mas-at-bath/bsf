package InstAL::Expr;
use OCC::Resolver;
use InstAL::CodeFragment;
use base InstAL::CodeFragment;


sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

1;

# by default we always assume that a mapping is valid
sub validMapping{
    return 1;
}

sub fillRefs{
    die("This should never happen");
}

sub is_static{
    return 1;
}
sub genASP{
    die("Badness");
}



