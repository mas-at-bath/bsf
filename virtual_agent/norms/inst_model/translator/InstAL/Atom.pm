package InstAL::Atom;

use OCC::Resolver;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub new {
    my $class = shift; 
    my $name = shift; 
    my $args = shift; 
    $args = [] if !$args;

    return bless {-name=>$name,
		   -args=>$args},$class;
}

1;
