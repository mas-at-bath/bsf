package InstAL::DynamicRule;

use InstAL::Rule;
use OCC::Resolver;
use ASP::Rule;
use vars qw( @ISA );
use UNIVERSAL;
use Data::Dumper;

#our @ISA;
@ISA = qw( InstAL::Rule );

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class = shift;
	my $head  = shift
	  ; # the event which causes the transition Must be a list of fluent refs all of which must be noninertial

	my $cond = shift;    # the condition expression an Expression or undef

	#	print STDERR "Head:", Dumper($head), "Cond ", Dumper($cond), "\n";
	my $self = bless {
		-head    => $head,                    #list of fluentRefs
		-cond    => $cond,                    #Expression
		-vars    => {},                       # var=>varref
		-varrefs => {},                       #var=>[target...]
		-name    => InstAL::Rule::newName()
	}, $class;

	return $self;
}

sub resolveVars {
	my $self = shift;
	foreach ( @{ $self->head } ) {
		$_->resolveVars($self);
	}
	$self->cond->resolveVars($self);
}

sub toString {
	my $self = shift;
	my $base = "always ";

	$base .= join( ",", ( map { $_->toString } ( @{ $self->head } ) ) ) . " ";

	if ( ref( $self->cond ) ne 'InstAL::TrueExpr' ) {
		$base .= "if " . $self->cond->toString;
	}
	return $base . ";\n";
}

sub resolveUsedLiterals {
	my $self = shift;
	my $lp   = InstAL::LiteralPool->lp;
	print STDERR "In resolve for dynamic rule!\n";
	print STDERR "Expanding used literals in ", $self->name, " with type",
	  $self->type, " \n"
	  if $DEBUG;
	return if $self->is_terminates;

	my $nvars = 0;
	if ( $nvars = values( %{ $self->vars } ) ) {
		my $mapping = InstAL::VarMapping->new( [ values( %{ $_->vars } ) ] );
		while ( $mapping->hasMore ) {
			if ( $self->cond->validMapping($mapping) ) {
				foreach ( @{ $self->head } ) {
					my $l = $_->resolveLiteral($mapping);
					$l->markUsed if $l;
				}
			}

			$mapping->nextMapping;
		}
	}
	else {
		foreach ( @{ $self->head } ) {
			my $l = $_->resolveLiteral(0);
			$l->markUsed if $l;
		}
	}
}

sub genASP {
	my $self    = shift;
	my $mapping = shift;
	my @rules;
	my @seenfluents;
	my ( $valid, $expr ) = $self->cond->validMapping($mapping);

	if ( !$valid ) {
		print STDERR $self->name . ":Mapping ", $mapping->toString,
		  " is eliminated by expr", $expr->toString, "\n"
		  if $DEBUG;
		return ( [], [] );
	}

	foreach my $headref ( ( @{ $self->head } ) ) {
		my $ha = $headref->innerASP($mapping);
		my @body;
		my $head = ASP::Atom->new( 'holdsat', [ $ha, ASP::Atom->new('I') ] );

		push @body, @{ $self->cond->genASP($mapping) };
		push @body, ASP::Atom->new( 'instant', [ ASP::Atom->new('I') ] );
		my $inst = $headref->fluent()->inst();
		push @body,
		  ASP::Atom->new(
			'holdsat',
			[
				ASP::Atom->new( 'live', [ ASP::Atom->new( $inst->name ) ] ),
				ASP::Atom->new('I')
			]
		  );
		push @rules, ASP::Rule->new( $head, \@body );

	}
	return ( \@rules, \@seenfluents );
}

