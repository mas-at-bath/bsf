package InstAL::Ref::FluentRef;
use InstAL::Ref::EventRef;
use OCC::Resolver;
use ASP::Atom;
use InstAL::LiteralPool;

use vars qw /@ISA/;
use InstAL::Ref;
use base 'InstAL::Ref';

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub is_static {
	my $self = shift;

	return $self->fluent->is_static;
}

sub is_noninertial {
	my $self = shift;
	return $self->fluent->is_noninertial;
}

sub new {
	my $class      = shift;
	my $fluentname = shift;
	my $args       = shift;
	my $fluent;

	if ( UNIVERSAL::isa( $fluentname, 'InstAL::Fluent' ) ) {
		$fluent     = $fluentname;
		$fluentname = $fluent->name;
	}
	my $self = {
		-fluent     => $fluent,
		-fluentname => $fluentname,
		-args       => $args,
		-static     => 0
	};

	return bless $self, $class;
}

sub fluent {
	my $self = shift;
	if ( !defined( $self->{-fluent} ) ) {
		my $fluent =
		  InstAL::LiteralPool->lp()->get_fluents( $self->fluentname() );
		if ( !defined($fluent) ) {
			die "Unable to find referenced fluent "
			  . $self->fluentname();
		}
		$self->{-fluent} = $fluent;
	}
	return $self->{-fluent};

}

sub getType {
	my $self = shift;
	return $self->fluent;
}

sub resolveVars {
	my $self = shift;
	my $rule = shift;

	if ( $self->is_static && !$self->fluent->is_static ) {
		$self->error( "Got non-static fluent ",
			$self->toString(), " where I was expecting a static fluent" );
	}
	my $nargs = @{ $self->args };
	for ( my $i = 0 ; $i < $nargs ; $i++ ) {
		$self->args->[$i] = $rule->add_varref( $self->args->[$i], $self );
		my $newref = $rule->add_varref( $self->get_args($i), $self );
		die if !$newref;
		$self->args->[$i] = $newref;
	}

}

sub getType {
	my $self = shift;

	return $self->fluent;
}

sub toString {
	my $self = shift;
	my $str  = $self->fluent->name;

	if ( @{ $self->args } ) {
		$str .= "("
		  . join( ",", ( map { $_->toString } ( @{ $self->args } ) ) ) . ")";
	}
	return $str;
}

sub innerASP {
	my $self    = shift;
	my $mapping = shift;

	my @aspargs = ();
	if ( @{ $self->args } ) {
		foreach ( @{ $self->args } ) {
			push @aspargs, $_->innerASP( $mapping, 1 );
		}
	}
	return ASP::Atom->new( $self->fluent->name, \@aspargs );

}

sub genASP {
	my $self    = shift;
	my $mapping = shift;
	return [
		ASP::Atom->new(
			'holdsat', [ $self->innerASP($mapping), ASP::Atom->new('I') ]
		)
	];

}
1;
