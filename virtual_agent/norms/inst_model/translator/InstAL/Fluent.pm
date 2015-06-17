package InstAL::Fluent;
use strict;
use InstAL::Type;
use InstAL::CodeFragment;
use InstAL::LiteralPool;
use InstAL::Ref::FluentRef;

use vars qw/ $AUTOLOAD $DEBUG/;
use base 'InstAL::Type';

use OCC::Resolver;

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class    = shift;
	my $name     = shift;
	my $flags    = shift;
	my $args     = shift;
	my $inst     = shift;
	my $internal = shift;

	my $static = 0;
	my $unique = 0;
	my $noninertial =0;
	
	foreach ( @{$flags} ) {
		if ( $_ eq 'static' ) {
			$static = 1;
		}
		elsif ( $_ eq 'unique' ) {
			$unique = 1;
		}elsif ($_ eq 'noninertial'){
			$noninertial = 1;
		}
	}

	my $lp = InstAL::LiteralPool->lp;

	my $super = $lp->typeFluent;

	my $self = bless {
		-name          => $name,
		-args          => $args,
		-static        => $static,
		-unique        => $unique,
		-internal      => $internal,
		-inst          => $inst,
		-super         => $super,
		-noninertial   => $noninertial,
		-literals      => [],
		-used_literals => {},
		-subtypes      => {}
	}, $class;

	my $lp = InstAL::LiteralPool->lp;
	$lp->set_types( $self->typeName, $self );
	return $self;
}

sub typeVarRefs {
	my $self = shift;
	return [] if !@{ $self->args };
	my @vars;
	my %vtypes;
	foreach ( @{ $self->args } ) {
		my $n = $vtypes{$_}++;
		my $vr = InstAL::Ref::VarRef->new( 'V' . $_->name . $n, $_ );
		$vr->copyLocFrom($self);
		push @vars, $vr;
	}
	return \@vars;
}

# returns a prototypical fluentref for this fluent for grounding
sub typeFluentRef {
	my $self = shift;
	my $vars = $self->typeVarRefs;

	return InstAL::Ref::FluentRef->new( $self, $vars );
}

sub addLiterals {
	my $self = shift;
	my $lp   = shift;

	print STDERR "Fluent: Adding possible literals for ", $self->name, "\n"
	  if $DEBUG;
	if ( !@{ $self->args } ) {
		my $fluentref = InstAL::Ref::FluentRef->new($self);
		$lp->add_literal( $fluentref->innerASP()->tostring, $self );
	}
	else {
		my $fluentref = $self->typeFluentRef;

		my $mapping = InstAL::VarMapping->new( $fluentref->args );
		while ( $mapping->hasMore ) {
			my $asp     = $fluentref->innerASP($mapping);
			my $literal = $asp->tostring;
			print "Adding $literal to ", $self->typeName, "\n" if $DEBUG;
			$lp->add_literal( $literal, $self );
			$mapping->nextMapping;
		}
	}
}

sub getTypeDeps {
	my $self = shift;
	return [ @{ $self->args } ];
}

sub typeName {
	my $self = shift;
	return 'FA_' . $self->name;
}

sub toString {
	my $self = shift;
	return "" if $self->is_internal;
	my $base =
	    ( $self->is_static ? "static " : "" )
	  . ( $self->is_unique ? "unique " : "" )
	  . "fluent "
	  . $self->name;
	if ( @{ $self->args } ) {
		$base .=
		  "(" . join( ",", map { $_->toString } ( @{ $self->args } ) ) . ")";
	}
	$base .= ";\n";
	return $base;
}
1;
