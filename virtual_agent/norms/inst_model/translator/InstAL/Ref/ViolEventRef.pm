package InstAL::Ref::ViolEventRef;
use OCC::Resolver;
use vars qw/$AUTOLOAD/;
use strict;
use ASP::Atom;
use InstAL::Ref;
use base 'InstAL::Ref::EventRef';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub new  {
    my $type = shift;
    my $eventname = shift;
    my $args = shift;
    $args =  [] if !$args;

    my $event;

    if(UNIVERSAL::isa($eventname,'InstAL::Event')){
	$event = $eventname;
	$eventname = $event->name;
    }


    my $self = {-event =>$event,
		-eventname=>$eventname,
		-args=>$args};
    return bless $self,$type;
}



sub toString{
    my $self = shift;
    
    my $name = $self->event->name;
    my $argstr ="";
    
    if(@{$self->args}){
	$argstr.= "(".
	    join(",",(map {$_->toString}(@{$self->args}))).")";
    }
    
    return "viol(".$self->baseName().$argstr.")";
}

#Filters out the viol
sub baseName{
    my $self = shift;
    my $name = $self->event->name;
    $name =~s/_viol_//;
    return $name;
}

#Just generates an atom for the event itself
sub innerASP{
    my $self = shift;
    my $mapping= shift;
    my @aspargs;
    foreach (@{$self->args}){
	push @aspargs, $_->innerASP($mapping);
    }

    my $name = $self->baseName;
    my $event  = ASP::Atom->new($name,\@aspargs);
    my $atom = ASP::Atom->new('viol',[$event]);
    return  $atom;
}


1;
