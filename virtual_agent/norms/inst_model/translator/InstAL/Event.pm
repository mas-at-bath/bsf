package InstAL::Event;
use ASP::Atom;
use OCC::Resolver;
use InstAL::VarMapping;
use InstAL::Ref::VarRef;
use InstAL::LiteralPool;
use InstAL::Type::PermType;
use InstAL::Type::PowType;

use vars qw/$DEBUG $AUTOLOAD/;
use strict;
use InstAL::CodeFragment;
use base 'InstAL::Type';

$DEBUG = $ENV{IALDBG};

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub INST {
	return 'inst';
}

sub EXO {
	return 'exogenous';
}

sub VIOL {
	return 'violation';
}

sub new {
	my $class    = shift;
	my $name     = shift;
	my $evtype   = shift;    #inst exo viol
	my $args     = shift;    #types
	my $inst     = shift;
	my $super    = shift;
	my $flags    = shift;
	my $internal = shift;    # don't display this when we print back
	my $lp = InstAL::LiteralPool->lp;

	$super = $lp->typeEvent() if !$super;

	my $self = bless {
		-name          => $name,
		-evtype        => $evtype,
		-args          => $args,
		-internal      => $internal,
		-inst          => $inst,
		-super         => $super,
		-literals      => [],
		-used_literals => {},
		-subtypes      => {},
		-flags         => $flags
	}, $class;

	my $permtype = new InstAL::Type::PermType($self);
	my $powtype  = new InstAL::Type::PowType($self);

	$lp->set_types( $self->typeName, $self );
	$lp->set_types( $powtype->name,  $powtype );
	$lp->set_types( $permtype->name, $permtype );

	if ($internal) {
		$self->{-loc_file} = "undefined";
		$self->{-loc_col}  = "0";
		$self->{-loc_line} = "0";

	}
	return $self;
}

sub permFluentName {
	my $self = shift;
	return '_PERM_' . $self->name;

}

sub powFluentName {
	my $self = shift;
	return '_POW_' . $self->name;
}

sub getTypeDeps {
	my $self = shift;
	return [ @{ $self->args } ];
}

sub typeName {
	my $self = shift;
	return 'EV_' . $self->name;
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

sub typeEvRef {
	my $self = shift;
	my $vars = $self->typeVarRefs;

	return InstAL::Ref::EventRef->new( $self, $vars );
}

sub is_exo {
	my $self = shift;
	return $self->evtype eq EXO;
}

#adds a literal to the pool for the event type for each possible
#mapping of each of the args called by  inst->resolveLiterals
sub addLiterals {
	my $self = shift;
	my $lp   = InstAL::LiteralPool->lp;

	print STDERR "Event: Adding Literals for ", $self->name, "\n" if $DEBUG;
	if ( !@{ $self->args } ) {
		my $evref   = InstAL::Ref::EventRef->new($self);
		my $literal = $evref->innerASP()->tostring;
		print STDERR "Adding $literal to ", $self->typeName, "\n" if $DEBUG;
		my $l = $lp->add_literal( $literal, $self );
		$l->markUsed if $l && $self->is_exo;
	}
	else {
		my $evref   = $self->typeEvRef;
		my $mapping = InstAL::VarMapping->new( $evref->args );
		while ( $mapping->hasMore ) {

			my $asp = $evref->innerASP($mapping);

			#WRONG WRONG WRONG ---
			my $literal   = $asp->tostring;
			my $entryname = $literal;
			if ( $literal =~ /^viol\((.*)\)$/ ) {
				$entryname = "_viol_$1";
			}

			print "Adding $literal to ", $self->typeName, "\n" if $DEBUG;
			my $l = $lp->add_literal( $literal, $self, $entryname );
			$l->markUsed if $l && $self->is_exo;
			$mapping->nextMapping;

			#WRONG WRONG WRONG
		}
	}
}

sub is_viol {
	my $self = shift;
	return $self->name =~ /^_viol_/;
}

sub literalName {
	my $self    = shift;
	my $mapping = shift;

	#    my $asp = $evref->innerASP($mapping);

}

sub innerToString {
	my $self = shift;
	my $str  = $self->name;
	if ( @{ $self->args } ) {
		$str .=
		  "(" . join( ",", map { $_->toString } ( @{ $self->args } ) ) . ")";
	}
	return $str;
}

sub toString {
	my $self = shift;
	return "" if $self->is_internal;
	return $self->evtype . " event " . $self->innerToString() . ";\n";

}

sub genASP {
	my $self    = shift;
	my $mapping = shift;

	my @asp;

	my $evatom;

	my @args;
	my $x = 0;

	foreach ( @{ $self->args } ) {
		push @args, ASP::Atom->new( $mapping->getVar( '#' . $x ) );
		$x++;
	}

	#     if($self->is_internal && $self->name=~/^_viol_(.*)$/){
	# 	my $event  = ASP::Atom->new($1,\@args);
	# 	$evatom = ASP::Atom->new('viol',[$event]);
	#     }else{
	# 	$evatom = ASP::Atom->new($self->name,\@args);
	#     }
	$evatom = ASP::Atom->new( $self->name, \@args );

	push @asp, ASP::Atom->new( 'event', [$evatom] );

	my $type = $self->evtype;
	$type = 'ex'   if $self->evtype eq $self->EXO;
	$type = 'viol' if $self->evtype eq $self->VIOL;

	if ( $self->evtype ne $self->VIOL ) {
		my $violatom =
		  ASP::Atom->new( 'viol', ASP::Atom->new( $self->name, \@args ) );
		push @asp,
		  ASP::Atom->new( 'evtype', [ $evatom, ASP::Atom->new('viol') ] );
	}

	push @asp, ASP::Atom->new( 'evtype', [ $evatom, ASP::Atom->new($type) ] );
	push @asp,
	  ASP::Atom->new( 'evinst',
		[ $evatom, ASP::Atom->new( $self->inst->varname ) ] );
	return [ map { ASP::Rule->new($_) } (@asp) ];

}

1;
