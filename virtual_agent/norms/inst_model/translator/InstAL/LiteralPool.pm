
package InstAL::LiteralPool;
use InstAL::Literal;
use vars qw/$DEBUG $AUTOLOAD/;
use OCC::Resolver;
use InstAL::VarMapping;
use InstAL::Type;
use UNIVERSAL;
use strict;

use vars
  qw/$evtype  $evtypetype $insttype $fluenttype $anytype $obltype  $permtype $powtype $violevtype $normfluenttype $domfluenttype $tlp/;

$DEBUG = $ENV{IALDBG};

sub lp {

	if ( !$tlp ) {
		$tlp = InstAL::LiteralPool->new();
		print STDERR "LP initialized\n" if $DEBUG;
		$tlp->init;
	}

	return $tlp;
}

sub BEGIN {
	my ( $lf, $ll, $lc ) = ( "none", "none", "none" );
	$anytype = InstAL::Type->new( 'ANY', 1 );
	$insttype   = new InstAL::Type( 'Inst',   1, $anytype );
	$evtype     = new InstAL::Type( 'Event',  1, $anytype );
	$evtypetype = new InstAL::Type( 'EvType', 1, $anytype );

	#    $instevtype = new InstAL::type('InstEvent',1,$evtype);
	$violevtype     = new InstAL::Type( 'ViolEvent',  1, $evtype );
	$fluenttype     = new InstAL::Type( 'Fluent',     1, $anytype );
	$domfluenttype  = new InstAL::Type( 'DomFluent',  1, $fluenttype );
	$normfluenttype = new InstAL::Type( 'NormFluent', 1, $fluenttype );

	$obltype  = new InstAL::Type( 'Obl',  1, $normfluenttype );
	$permtype = new InstAL::Type( 'Perm', 1, $normfluenttype );
	$powtype  = new InstAL::Type( 'Pow',  1, $normfluenttype );

}

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class = shift;

	my $self = bless {
		literals    => {},
		-types      => {},
		-fluents    => {},
		-events     => {},
		-insts      => {},
		-evrefs     => [],
		-fluentrefs => [],
		-lines      => {}
	}, $class;
	return $self;
}

sub typeANY {
	return $anytype;
}

sub typeEvent {
	return $evtype;
}

sub typeInst {
	return $insttype;
}

sub typeViolEvent {
	return $violevtype;
}

sub typeFluent {
	return $fluenttype;
}

sub typeNormFluent {
	return $normfluenttype;
} 
sub typeDomFluent {
	return $domfluenttype;
}

sub typeObl {
	return $obltype;
}

sub typePerm {
	
	return $permtype;
}

sub typePow {
	return $powtype;
}

sub addType {
	my $self = shift;
	my $type = shift;

	print STDERR "Adding type ", $type->typeName, "\n" if $DEBUG;

	if ( $type->super ) {
		die "BAAAD" if $type->super == $type;

		$type->super->set_subtypes( $type->typeName, $type );
		print STDERR "adding ", $type->typeName, " to subtypes of ",
		  $type->super->typeName, "\n"
		  if $DEBUG;
	}

	$self->{-types}->{ $type->typeName } = $type;
	return $type;
}

sub get_types {
	my $self   = shift;
	my $tn     = shift;
	my $cbnull = shift;

	my $type = $self->{-types}->{$tn};
	if ( !$type ) {
		die "$self Tried to resolve unknown type '$tn'" if !$type && !$cbnull;
	}

	return $type;
}

sub init {
	my $self = shift;

	# static internal event types
	$self->addType($anytype);
	$self->addType($evtype);
	$self->addType($violevtype);

	#    $self->addType($instevtype);
	$self->addType($insttype);
	$self->addType($fluenttype);
	$self->addType($normfluenttype);
	$self->addType($domfluenttype);

	$self->addType($evtypetype);
	$self->addType($obltype);
	$self->addType($permtype);
	$self->addType($powtype);

	$self->add_literal( 'act',  $evtypetype );
	$self->add_literal( 'ex',   $evtypetype );
	$self->add_literal( 'viol', $evtypetype );

	#static internal fluent types for obl,pow,perm etc
	my $oblfluent =
	  InstAL::Fluent->new( 'obl', [], [ $evtype, $evtype, $evtype ], 1 );
	$self->set_fluents( 'obl', $oblfluent );
	my $permfluent = InstAL::Fluent->new( 'perm', [], [$evtype], 1 );
	$self->set_fluents( 'perm', $permfluent );
	my $powfluent = InstAL::Fluent->new( 'pow', [], [ $insttype, $evtype ], 1 );
	$self->set_fluents( 'pow', $powfluent );
	my $eqfluent = InstAL::Fluent->new( 'eq', [], [ $anytype, $anytype ], 1 );
	$self->set_fluents( 'eq', $eqfluent );

	my $evfluent = InstAL::Fluent->new( 'event', [], [$evtype] );
	$evfluent->set_static(1);
	$self->set_fluents( 'event', $evfluent );

	my $evtypefluent = InstAL::Fluent->new(
		'evtype',
		[ 'static', 'unique' ],
		[ $evtype,  $evtypetype ]
	);
	$evtypefluent->set_static(1);

	$self->set_fluents( 'evtype', $evtypefluent );

	my $evinstfluent = InstAL::Fluent->new(
		'evinst',
		[ 'static', 'unique' ],
		[ $evtype,  $insttype ]
	);
	$evinstfluent->set_static(1);
	$self->set_fluents( 'evinst', $evinstfluent );

	my $livefluent = InstAL::Fluent->new( 'live', [], [$insttype] );
	$self->set_fluents( 'live', $livefluent );
	$self->addType($livefluent);    
	return $self;
}

sub loadStream {
	my $self   = shift;
	my $stream = shift;
	my $l;
	my $ln = 0;
  LOOP: while ( $l = <$stream> ) {
		$ln++;
		chomp $l;
		$l =~ s/%.*$//;
		next LOOP if ( !$l );

		if ( !( $l =~ /^([A-Z][A-Za-z0-9_]*):\s*(.*)$/ ) ) {
			die "Invalid literal pool, line $ln";
		}

		my $typename = $1;
		my $rest     = $2;
		$rest =~ s/\s+/ /g;
		print "Loading type: $typename\n" if $DEBUG;
		if ( !$self->types->{$typename} ) {
			$self->warn(
"Type $typename is in grounding file but not referenced in current model, skipping \n"
			);
			next LOOP;
		}
		my $type = $self->get_types($typename);
		die "Type $typename not found" if !$type;
		my @atoms = split /\s+/, $rest;
		foreach (@atoms) {
			chomp;
			$self->error( "Duplicate atom " . $_ )
			  if $self->get_literal( $_, 1 );
			my $l = $self->add_literal( $_, $type );
			$l->markUsed();

			print "Processed ", $type->name, ":", $_, "\n" if $DEBUG;
		}

	}
}

sub add_literal {
	my $self      = shift;
	my $name      = shift;
	my $type      = shift or die "Literal $name with no type";
	my $entryname = shift;
	die "Bad type " if !$type->isa('InstAL::Type');
	$entryname = $name if !$entryname;
	print STDERR "About to add $entryname to ", $type->typeName, "\n" if $DEBUG;

	#HACK TO GET AROUND GENERATING UNNECESSARY PERMISSIONS/POWERS
	if ( $type == $self->typePerm ) {
		$name =~ /^perm\((.*)\)$/;

		my $evname = $1;
		my $elit = $self->get_literal( $evname, 1 );
		die "No corresponding literal $evname for $name" if !$elit;
		print STDERR "** Checking $name ==   $evname:",
		  $type->typeName . ", if is a viol literal\n" if $DEBUG;
		my $evtype = $elit->type;
		if ( $evtype->isSubType( $self->typeViolEvent ) ) {
			print STDERR "Skipping $name:  $evname\n"if $DEBUG;
			return;
		}
	}

	if ( $type == $self->typePow ) {
		$name =~ /^pow\([^,]+,(.*)\)$/;

		my $evname = $1;
		my $elit   = $self->get_literal( $evname, 1 );
		my $evtype = $elit->type;
		die "No corresponding literal $evname for $name" if !$elit;
		print STDERR "** Checking $name == $evname (", $evtype->typeName, ") :",
		  $type->typeName . ", if is an obs literal\n" if $DEBUG;

		if ( $evtype->evtype ne InstAL::Event::INST() ) {
			print STDERR "Skipping $name:  $evname\n" if $DEBUG	;
			return;
		}
	}

	#END HACK
	my $lit = $self->get_literal( $entryname, 1 );
	return $lit if $lit;

	print STDERR "adding $name:", $type->typeName, " to pool as $entryname \n"  if $DEBUG;
	my $l = InstAL::Literal->new( $name, $type );
	$self->{literals}->{$entryname} = $l;
	$type->add_literals($l);
	return $l;
}

sub addInst {
	my $self = shift;
	my $inst = shift;
	$self->set_insts( $inst->name, $inst );
	$self->add_literal( $inst->name, $self->get_types('Inst') )->markUsed;
	$self->add_literal( "live(" . $inst->name . ")",
		$self->get_fluents('live') )->markUsed;

}

sub toString {
	my $self    = shift;
	my $str     = shift;
	my $anytype = $self->get_types('ANY');

#print STDERR  "ALL Literals: ", join(",",map {$_->name}(@{$anytype->getAllLiterals})),"\n";
	return $anytype->dbgToString("");

	foreach ( map { $self->get_literal($_) }
		( sort { $a cmp $b } ( keys %{ $self->{literals} } ) ) )
	{
		$str .= $_->name . ":" . $_->type->name . "\n";
	}

	foreach ( map { $self->get_events($_) }
		( sort { $a cmp $b } ( keys %{ $self->events } ) ) )
	{
		$str .= $_->name . "\n";

	}

	return $str;
}

sub get_literal {
	my $self   = shift;
	my $name   = shift;
	my $cbnull = shift;
	$self->error("Literal $name is not presentent")
	  if !$cbnull && !$self->{literals}->{$name};

	return $self->{literals}->{$name};
}

sub error {
	my $self = shift;
	my $msg  = shift;
	my $loc  = shift;

	my $line;
	my $col;
	my $file;

	if ($loc) {
		$msg .= " (at " . $loc->getLocStr . ")";
		$line = $loc->loc_line;
		$col  = $loc->loc_col;
		$file = $loc->loc_file;
	}

	my $frag;
	if ( $file && $line && $col ) {

		#	print "looking for $file:$col:$line\n";
		my $i;
		my $lines = $self->get_lines($file);

		print STDERR "looking for $file $line\n" if $DEBUG;
		$frag = $self->get_lines($file)->[ int($line) - 1 ];

		if ( $col > 0 ) {
			$frag .= "\n";
			$frag .= "-" x ( $col - 1 ) . "^";
		}
	}

	die("ERROR:$msg\n$frag\n");

}

# returns all of the literal of a type (including its sub types)

sub warn {
	my $self = shift;
	my $msg  = shift;
	my $loc  = shift;

	if ($loc) {
		$msg .= " (at " . $loc->getLocStr . ")";
	}
	print STDERR "WARNING: $msg\n";
}

# Stage 2: resolve any fluent/event references
sub resolveReferences {
	my $self = shift;

	foreach ( @{ $self->evrefs } ) {
		my $evref  = $_;
		my $evname = $evref->eventname;
		my $ev     = $self->get_events($evname);

		$self->error( "Unknown event $evname at ", $evref ) if !$ev;

		my $nargs   = @{ $evref->args };
		my $expargs = @{ $ev->args };

		$self->error(
"Incorrect number of arguments to $evname (Got $nargs, expected $expargs)",
			$evref
		  )
		  if $nargs != $expargs;
		my @refargs;

		foreach ( 0 .. ( $nargs - 1 ) ) {
			my $arg = $evref->get_args($_);
			die "ERROR:empty arg $_" if !$arg;
			my $fa = $ev->get_args($_);
			print "Checking $evname  [$_]:$arg\n" if $DEBUG;
			if ( UNIVERSAL::isa( $arg, 'InstAL::Ref::VarRef' ) ) {
				print "remapping ", $arg->var, " to ", $fa->name, "\n"
				  if $DEBUG;
				$arg->set_type($fa);
			}
		}

		$evref->set_event($ev);
		print "resolved ", $evref->toString, " (", $evref->getLocStr(), ")-->",
		  $ev->innerToString(), "\n"
		  if $DEBUG;
	}

	foreach ( @{ $self->fluentrefs } ) {
		my $fluentref  = $_;
		my $fluentname = $fluentref->fluentname;
		my $fluent     = $self->get_fluents($fluentname);

		$self->error( "Unable to resolve fluent $fluentname", $fluentref )
		  if !$fluent;

		my $nargs   = @{ $fluentref->args };
		my $expargs = @{ $fluent->args };

		die
"Incorrect number of arguments to $fluentname (Got $nargs, expected $expargs)"
		  if $nargs != $expargs;
		my @refargs;
		foreach ( 0 .. ( $nargs - 1 ) ) {
			my $arg = $fluentref->args->[$_];
			die "ERROR:empty arg $_" if !$arg;
			my $fa = $fluent->get_args($_);
			print "Checking $fluentname [$_]:$arg\n" if $DEBUG;
			if ( UNIVERSAL::isa( $arg, 'InstAL::Ref::VarRef' ) ) {
				print "remapping ", $arg->var, " to ", $fa->name, "\n"
				  if $DEBUG;
				$arg->set_type($fa);
			}
		}
		$fluentref->set_fluent($fluent);
	}

}

sub checkTypeReferences {

}
1;
