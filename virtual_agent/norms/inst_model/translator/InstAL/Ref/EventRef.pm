package InstAL::Ref::EventRef;
use OCC::Resolver;
use vars qw/$AUTOLOAD/;
use strict;
use ASP::Atom;
use InstAL::Ref;
use InstAL::LiteralPool;
use base 'InstAL::Ref';

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class     = shift;
	my $eventname = shift;
	my $args      = shift;
	$args = [] if !$args;

	my $event;

	if ( UNIVERSAL::isa( $eventname, 'InstAL::Event' ) ) {
		$event     = $eventname;
		$eventname = $event->name;
	}

	my $self = {
		-event     => $event,
		-eventname => $eventname,
		-args      => $args
	};
	return bless $self, $class;
}

sub resolveVars {
	my $self = shift;
	my $rule = shift;

	#    print "self->args is ",$self->args,"\n";
	my $nargs = @{ $self->args };
	for ( my $i = 0 ; $i < $nargs ; $i++ ) {
		my $newref = $rule->add_varref( $self->get_args($i), $self );
		die if !$newref;
		$self->args->[$i] = $newref;
	}

}

sub toString {
	my $self = shift;

	my $name   = $self->event->name;
	my $argstr = "";

	if ( @{ $self->args } ) {
		$argstr .= "("
		  . join( ",", ( map { $_->toString } ( @{ $self->args } ) ) ) . ")";
	}

	return $name . $argstr;

}

#Just generates an atom for the event itself
sub innerASP {
	my $self    = shift;
	my $mapping = shift;
	my @aspargs;
	foreach ( @{ $self->args } ) {
		push @aspargs, $_->innerASP($mapping);
	}

	#evil hackery to get around having to render occured(viol(e),I) properly
	return ASP::Atom->new( $self->event->name, \@aspargs );

}

#HACK to overcome event typing with event variables @see InstAL::EventVar::event
sub event {
	my $self = shift;
	if ( !defined( $self->{-event} ) ) {
		my $event =
		  InstAL::LiteralPool->lp()->get_events( $self->{-eventname} );
		if ( !defined($event) ) {
			die "Unable to resolve event named " . $self->{-eventname};
		}
		$self->{-event} = $event;
	}
	return $self->{-event};
}

sub getType {
	my $self = shift;
	return $self->event;
}

#generates an occured atom for the event;
sub genASP {
	my $self    = shift;
	my $mapping = shift;
	my $ia      = $self->innerASP($mapping);
	return [ ASP::Atom->new( 'occured', [ $ia, ASP::Atom->new('I') ] ) ];
}

sub genObsASP {
	my $self    = shift;
	my $mapping = shift;

	my $ia = $self->innerASP($mapping);

	return [ ASP::Atom->new( 'observed', [ $ia, ASP::Atom->new('I') ] ) ];

}

sub powFluent {
	my $self = shift;
}
1;
