package InstAL::MultiGeneratesRule;

=head1 InstAl::MultiGeneratesRule
A generates rule occuring in a multi-institution definition 
=cut

use OCC::Resolver;
use InstAL::Rule;
use InstAL::Ref::LiteralRef;
use vars qw( @ISA  $AUTOLOAD $DEBUG);
use strict;
$DEBUG = $ENV{IALDBG};
@ISA   = qw(InstAL::GeneratesRule);
use Data::Dumper;

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

# types 0: initiates 1: termiates
sub new {
	my $class = shift;
	my $event = shift;    # the event which causes the transition
	my $gens  = shift;    # the generated events EventExprs
	my $cond  = shift;    # the condition expression an Expression or undef

	my $self = bless {
		-event   => $event,                   #eventref
		-gens    => $gens,                    #[EventRefs]
		-cond    => $cond,                    #Expression
		-vars    => {},                       # var=>type
		-varrefs => {},                       #var=>[target...]
		-name    => InstAL::Rule::newName()
	}, $class;

	return $self;
}

sub genASP {
	my $self    = shift;
	my $mapping = shift;
	my $lp      = InstAL::LiteralPool->lp;
	die('badness') if not $lp;

	my ( $valid, $expr ) = $self->cond->validMapping($mapping);

	if ( !$valid ) {
		print "Mapping ", $mapping->toString, " is eliminated by expr ",
		  $expr->toString, "\n"
		  if $DEBUG;
		return ( [], [] );
	}
	my @rules;

	# just generate (obs(e) :- occ(f),TRANS(X))
	foreach ( @{ $self->gens } ) {
		my $ev = $_->event( $mapping, $lp );
		my ($head) = @{ $_->genObsASP($mapping) };
		my @body;

		push @body,
		  @{
			$self->event->genASP( $mapping,
				InstAL::LiteralPool->lp->get_types('Event') )
		  };
		push @body, @{ $self->cond->genASP($mapping) };

		push @body, ASP::Atom->new( 'instant', [ ASP::Atom->new('I') ] );

	 #	print "ASP ",ASP::Rule->new(ASP::Atom->new("foo"),\@body)->toString,"\n";
		push @rules, ASP::Rule->new( $head, \@body );
	}
	return \@rules;

}
