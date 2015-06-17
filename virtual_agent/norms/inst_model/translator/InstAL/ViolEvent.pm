package InstAL::ViolEvent;
use ASP::Atom;
use OCC::Resolver;

use vars qw/$DEBUG $AUTOLOAD/;
use strict;
use InstAL::Event;
use base 'InstAL::Event' ;


$DEBUG = $ENV{IALDBG};
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub new {
    my $class = shift;
    my $event = shift;
    my $inst = shift;
    my $super= shift;
    my $lp = InstAL::LiteralPool->lp;
    my $super = $lp->typeViolEvent if !$super;

    my $self =  bless {-name=>'_viol_'.$event->name,
		       -evtype=>InstAL::Event->VIOL(),
		       -event=>$event, 
		       -args =>$event->args,
		       -internal=>1,
		       -inst=>$inst,
		       -super=>$super,
		       -literals=>[],
		       -subtypes=>{},
		       -flags =>{}
		       
		   },$class;

    return  $self;
}

sub typeName{
    my $self = shift;
    return 'EV_VIOL_'. $self->name;
    
}
1;
