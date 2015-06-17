package InstAL::Ref::LiteralRef;

use OCC::Resolver;
use ASP::Atom;
use InstAL::Ref;
use base InstAL::Ref;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new{
    my $class = shift;
    my $name = shift;
    my $type = shift;
    
    my $self = {
	-name=>$name,
	-type=>$type,
    };
    
    return bless $self,$class;
}

sub toString{
    my $self = shift;
    return $self->name;
}


sub innerASP{
    my $self = shift;
    my $mapping = shift;
    return ASP::Atom->new($self->name);     
}
1;
