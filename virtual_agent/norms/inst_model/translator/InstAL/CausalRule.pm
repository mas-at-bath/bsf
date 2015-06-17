package InstAL::CausalRule;
use InstAL::Rule;
use OCC::Resolver;
use ASP::Rule;
use vars qw( @ISA );

#our @ISA;
@ISA = qw( InstAL::Rule );

sub INITIATES {
	return 0;
}

sub TERMINATES {
	return 1;
}

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

# types 0: initiates 1: termiates
sub new {
	my $class = shift;
	my $type  = shift;
	my $event = shift;    # the event which causes the transition
	my $cons  = shift;    # the consequences of the transition list of FactExprs
	my $cond  = shift;    # the condition expression an Expression or undef

	my $self = bless {
		-type    => int($type),                #1|0
		-event   => $event,                    #eventref
		-cons    => $cons,                     #[fluentprops]
		-cond    => $cond,                     #Expression
		-vars    => {},                        # var=>varref
		-varrefs => {},                        #var=>[target...]
		-name    => InstAL::Rule::newName(),
	}, $class;

	return $self;
}

sub is_initiates {
	my $self = shift;
	return !$self->is_terminates;
}

sub is_terminates {
	my $self = shift;
	return $self->type == TERMINATES();
}

sub resolveVars {
	my $self = shift;

	$self->event->resolveVars($self);
	foreach ( @{ $self->cons } ) {
		$_->resolveVars($self);
	}
	$self->cond->resolveVars($self);
}

sub toString {
	my $self = shift;
	my $base = $self->event->toString;
	$base .= " " . ( $self->is_terminates ? "terminates" : "initiates" ) . " ";
	$base .= join( ",", ( map { $_->toString } ( @{ $self->cons } ) ) ) . " ";

	if ( ref( $self->cond ) ne 'InstAL::TrueExpr' ) {
		$base .= "if " . $self->cond->toString;
	}
	return $base . ";\n";
}

sub resolveUsedLiterals {
	my $self = shift;
	my $lp   = InstAL::LiteralPool->lp;

	print STDERR "Expanding used literals in ", $self->name, " with type",
	  $self->type, " \n"
	  if $DEBUG;
	return if $self->is_terminates;

	my $nvars = 0;
	if ( $nvars = values( %{ $self->vars } ) ) {
		my $mapping = InstAL::VarMapping->new( [ values( %{ $_->vars } ) ] );
		while ( $mapping->hasMore ) {
			if ( $self->cond->validMapping($mapping) ) {
				foreach ( @{ $self->cons } ) {
					my $l = $_->resolveLiteral($mapping);
					$l->markUsed if $l;
				}
			}

			$mapping->nextMapping;
		}
	}
	else {
		foreach ( @{ $self->cons } ) {
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

	my @extra;

	if (!$self->event->event($mapping)->flags->{create})
	{
		my $inst = $self->event->event($mapping)->inst();    
		push @extra,
		  ASP::Atom->new(
			'holdsat',
			[
				ASP::Atom->new( 'live', [ ASP::Atom->new( $inst->name ) ] ),
				ASP::Atom->new('I')
			]
		  );
	}
	if ( !$valid ) {
		print STDERR $self->name . ": Mapping ", $mapping->toString,
		  " is eliminated by expr", $expr->toString, "\n"
		  if $DEBUG;
		return ( [], [] );
	}
	foreach ( @{ $self->cons } ) {

		my $ha = $_->innerASP($mapping);
		my $head;
		push @seenfluents, $ha;
		if ( $self->is_initiates ) {
			$head = ASP::Atom->new( 'initiated', [ $ha, ASP::Atom->new('I') ] );
		}
		else {
			$head =
			  ASP::Atom->new( 'terminated', [ $ha, ASP::Atom->new('I') ] );
		}
		my @body;
		push @body,  @{ $self->event->genASP($mapping) };
		push @body,  @{ $self->cond->genASP($mapping) };
		push @body,  @extra;
		push @body,  ASP::Atom->new( 'instant', [ ASP::Atom->new('I') ] );
		push @rules, ASP::Rule->new( $head, \@body );
	}
	return \@rules;
}

# sub add_varref{
#     my $self = shift;

#     $self->SUPER::add_varref(@_);
# }
