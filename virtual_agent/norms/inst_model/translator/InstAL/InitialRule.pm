package InstAL::InitialRule;

use OCC::Resolver;
use InstAL::Rule;
use vars qw( @ISA  $AUTOLOAD $DEBUG);
use strict;
@ISA = qw(InstAL::Rule);

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

# types 0: initiates 1: termiates
sub new {
	my $class      = shift;
	my $fluentrefs = shift;    # [Factrefs]
	my $cond       = shift;    # [Expr]

	my $self = bless {
		-fluentrefs => $fluentrefs,              #[fluentrefs]
		-cond       => $cond,                    #Expression
		-vars       => {},                       # var=>type
		-varrefs    => {},                       #var=>[target...]
		-name       => InstAL::Rule::newName()
	}, $class;

	return $self;
}

sub resolveVars {
	my $self = shift;

	foreach ( @{ $self->fluentrefs } ) {
		$_->resolveVars($self);
	}

	$self->error("Cannot use a non-static expression in initially")
	  if !$self->cond->is_static;

	$self->cond->resolveVars($self);
}

sub resolveUsedLiterals {
	die "Should neve resolve used literals on initiallys\n";
}

sub resolveInitialLiterals {
	my $self         = shift;
	my $litearalpool = shift;
	my @varrefs      = values %{ $self->vars };
	my $nvars        = @varrefs;

	if ( !$nvars ) {
		print STDERR "Rule ", $self->name, " Has no vars\n" if $DEBUG;
		foreach ( @{ $self->fluentrefs } ) {
			my $l = $_->resolveLiteral;
			$l->markUsed() if $l;
		}
	}
	else {
		print STDERR "Rule ", $self->name, " Has $nvars vars: @varrefs\n"
		  if $DEBUG;
		my $vm = new InstAL::VarMapping( \@varrefs );
		while ( $vm->hasMore ) {
			if ( $self->cond->validMapping($vm) ) {
				foreach ( @{ $self->fluentrefs } ) {
					my $l = $_->resolveLiteral($vm);
					$l->markUsed() if $l;
				}
			}
			$vm->nextMapping;
		}
	}
}

sub toString {
	my $self = shift;
	my $base = "initially ";

	$base .= join( ",", ( map { $_->toString } ( @{ $self->fluentrefs } ) ) );

	if ( ref( $self->cond ) ne 'InstAL::TrueExpr' ) {
		$base .= " if " . $self->cond->toString;
	}
	return $base . ";\n";

}

sub genASP {
	my $self    = shift;
	my $mapping = shift;
	my $lp      = shift;
	my $inst    = shift or die "called without inst";

	my ( $valid, $expr ) = $self->cond->validMapping($mapping);

	if ( !$valid ) {
		print "Mapping ", $mapping->toString, " is eliminated by expr ",
		  $expr->toString, "\n"
		  if $DEBUG;
		return ( [], [] );
	}
	my @usedfluents;
	my @rules;

# for each craeat event in this inst, and for each mapping we generate an initiated
	my @extracond;
	push @extracond, new ASP::Atom(
		'holdsat',
		[
			new ASP::Atom( 'live', [ new ASP::Atom( $inst->name ) ] ),
			new ASP::Atom('I')
		]
	);

	$extracond[0]->add_tag('NOT');
	push @extracond, new ASP::Atom( 'instant', [ new ASP::Atom('I') ] );

	foreach
	  my $lit ( @{ InstAL::LiteralPool->lp->typeEvent->getAllUsedLiterals } )
	{
		my $event = $lit->type;
		my $evcond =
		  new ASP::Atom( 'occured', [ $lit->atom, new ASP::Atom('I') ] );

		#		print "CHECKING ", $lit->name, "\n";
		if ( $event->inst == $inst && $event->flags->{'create'} ) {
			foreach ( @{ $self->fluentrefs } ) {
				my $ha = $_->innerASP($mapping);
				push @usedfluents, $ha;
				my @body;
				push @body, @{ $self->cond->genASP($mapping) };
				push @body, $evcond;
				push @body, @extracond;
				my $head =
				  ASP::Atom->new( 'initiated', [ $ha, ASP::Atom->new('I') ] );
				push @rules, ASP::Rule->new( $head, \@body );
			}
		}

	}
	return \@rules;

}
