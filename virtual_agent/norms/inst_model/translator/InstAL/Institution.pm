package InstAL::Institution;
use strict;
use OCC::Resolver;
use InstAL::Fluent;
use InstAL::Type;
use InstAL::VarMapping;
use InstAL::LiteralPool;
use ASP::Atom;
use ASP::Rule;
use ASP::Comment;
use vars qw/$AUTOLOAD $DEBUG/;
$DEBUG = $ENV{IALDBG};

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );

}

sub new {
	my $class = shift;
	my $lp    = shift or die "No literal pool";
	my $self  = bless {
		-name        => "none",
		-flags       => [],
		-symboltable => {},
		-initiallys  => [],
		-genrules    => [],
		-dynamics    => [],
		-initrules   => [],
		-termrules   => [],
		-types       => {},
		-fluents     => {},
		-events      => {},
		-lp          => $lp

	}, $class;

	return $self;
}

sub varname {
	my $self = shift;
	my $name = $self->name;
	$name =~ tr/[A-Z]/[a-z]/;
	return $name;
}

sub add_type {
	my $self = shift;
	my $type = shift;
	my $name = $type->name;
	print "Adding type $name\n" if $DEBUG;
	$type->error("Type $name already defined") if $self->get_types($name);
	my $newtype;
	my $lptype = $self->lp->{-types}->{$name};
	if ( !$lptype ) {
		$newtype = new InstAL::Type( $name, 0, $self->lp->get_types('ANY') );
		$self->lp->addType($newtype);
	}

	$self->set_types( $name, $newtype );
	return $newtype;
}

sub resolveVars {
	my $self = shift;
	foreach ( @{ $self->initiallys } ) {
		print STDERR ">>resolving intitiallys in ", $self->name, "\n" if $DEBUG;
		$_->resolveVars;
		print STDERR "<<resolved intitiallys in ", $self->name, "\n" if $DEBUG;
	}
	foreach ( @{ $self->initrules } ) {
		print STDERR ">>resolving initiates in ", $self->name, "\n" if $DEBUG;
		$_->resolveVars;
		print STDERR ">>resolved initiates in ", $self->name, "\n" if $DEBUG;
	}
	foreach ( @{ $self->termrules } ) {
		print STDERR ">>resolving terminates in ", $self->name, "\n" if $DEBUG;
		$_->resolveVars;
		print STDERR "<<resolved terminates in ", $self->name, "\n" if $DEBUG;

	}
	foreach ( @{ $self->genrules } ) {
		print STDERR ">>resolving generates in ", $self->name, "\n" if $DEBUG;
		$_->resolveVars;
		print STDERR "<<resolved generates in ", $self->name, "\n" if $DEBUG;
	}

	foreach ( @{ $self->dynamics } ) {
		print STDERR ">>resolving dynamics in ", $self->name, "\n" if $DEBUG;
		$_->resolveVars;
		print STDERR "<<resolved dynamci in ", $self->name, "\n" if $DEBUG;
	}
}

# A  fluent literal is present iff:
#  - it is initiallys
#  - if it is initiated by a rule with a valid mapping

#  - a rule has a valid condition  if its condition matches the current fluent base;

# an event literal is present iff:
#  -- if it is the generate conditions of a generates rule and that rule has a matchin condition;

sub resolveBaseLiterals {
	my $self = shift;
	my $lp   = shift;

	my $l = $lp->add_literal( $self->name, $lp->get_types('Inst') );
	$l->markUsed();

	print STDERR "Resolving base literals for ", $self->name, "\n" if $DEBUG;
	my $nliterals = 0;
	my $newlits   = @{ $lp->typeEvent->getAllLiterals() };
	my $i         = 0;

	do {
		foreach ( map { $self->get_events($_) } ( keys( %{ $self->events } ) ) )
		{
			$_->addLiterals($lp);
		}

		$nliterals = $newlits;
		$newlits   = @{ $lp->typeEvent->getAllLiterals() };
		print STDERR "Event pass $i ($newlits: $nliterals)\n" if $DEBUG;
		$i++;
	} while ( $newlits > $nliterals );

	foreach ( map { $self->get_fluents($_) } ( keys( %{ $self->fluents } ) ) ) {
		$_->addLiterals($lp);
	}
}

sub resolveInitialUsedLiterals {
	my $self = shift;
	my $lp   = shift;

	foreach ( @{ $self->initiallys } ) {
		print STDERR "Resolving literals for ", $_->name, "\n" if $DEBUG;
		$_->resolveInitialLiterals;
	}

}

sub resolveUsedLiterals {
	my $self = shift;
	my @allrules;

	# live fluent is always used;
	push @allrules, @{ $self->initrules };
	push @allrules, @{ $self->termrules };
	push @allrules, @{ $self->genrules };

	foreach (@allrules) {
		$_->resolveUsedLiterals();
	}
}

sub toString {
	my $self = shift;
	my $str  = "institution " . $self->name . ";\n";

	$str .= "\n%Flags\n";

	foreach ( @{ $self->flags } ) {
		$str .= $_->[0] . " " . $_->[1] . ";\n";
	}

	$str .= "\n%Types\n";

	foreach ( map { $self->get_types($_) }
		( sort { $a cmp $b } keys( %{ $self->types } ) ) )
	{
		$str .= $_->toDefString;
	}
	$str .= "\n%Facts\n";

	foreach ( map { $self->get_fluents($_) }
		( sort { $a cmp $b } keys( %{ $self->fluents } ) ) )
	{
		$str .= $_->toString;
	}

	$str .= "\n%Events\n";
	foreach ( map { $self->get_events($_) }
		( sort { $a cmp $b } keys( %{ $self->events } ) ) )
	{
		$str .= $_->toString;
	}

	$str .= "\n%Rules\n";

	foreach ( @{ $self->initrules } ) {
		$str .= $_->toString;
	}
	foreach ( @{ $self->termrules } ) {
		$str .= $_->toString;
	}
	foreach ( @{ $self->genrules } ) {
		$str .= $_->toString;
	}

	foreach ( @{ $self->dynamics } ) {
		$str .= $_->toString;
	}

	$str .= "\n%Initial fluents\n";
	foreach ( @{ $self->initiallys } ) {
		$str .= $_->toString;
	}

	#     $str.="\n%Inits\n";
	#     foreach (@{$self->initiallys}){
	# 	$str.=$_->toString;
	#     }
	return $str;

}

sub oblAtom {
	my $self = shift;
	my $atom = shift;
	$atom->name eq 'obl';
}

sub name {
	my $self = shift;
	return lc( $self->{-name} );
}

sub generateASP {
	my $self = shift;

	my @rules;

	my @allrules;
	my $lp = InstAL::LiteralPool->lp;

	push @allrules, @{ $self->initrules };
	push @allrules, @{ $self->termrules };
	push @allrules, @{ $self->genrules };
	push @allrules, @{ $self->dynamics };
	push @allrules, @{ $self->initiallys };

	my $instrule =
	  new ASP::Rule(
		new ASP::Atom( 'inst', [ new ASP::Atom( $self->name ) ] ) );
	$instrule->set_comment(
		"\n\nRules for Institution " . $self->name . "\n\n" );
	push @rules, $instrule;
	foreach (@allrules) {
		print STDERR ">>Generating rule ", $_->name, ":
	", $_->toString, "\n" if $DEBUG;
		push @rules, new ASP::Comment( "Translation of: \n" . $_->toString() );
		my @vars = values( %{ $_->vars } );
		print STDERR "My vars ar [",
		  join( ",", map { $_->var . ":" . $_->type->name } (@vars) ), "]\n"
		  if $DEBUG;
		if (@vars) {
			my $vm = InstAL::VarMapping->new( [ values( %{ $_->vars } ) ], 1 );
			print STDERR "&&&&& ", $vm->toString(), "\n" if $DEBUG;
			while ( $vm->hasMore ) {

				#	print "Calling",$vm, $self->lp, $self ,"\n";

				my ( $asp, $fluents ) = $_->genASP( $vm, $self->lp, $self );
				print STDERR "Expanding ", $vm->toString, "\n" if $DEBUG;
				push @rules, @{$asp};

				$vm->nextMapping;
			}
		}
		else {
			my ( $asp, $fluents ) = $_->genASP( undef, undef, $self );
			push @rules, @{$asp};

		}

	}

	#     foreach (map {$self->get_events($_)}
	# 	     (sort {$a cmp $b} keys(%{$self->events}))){
	# 	my $x;
	# 	if(@{$_->args}){
	# 	    my @vrs = map{InstAL::VarRef->new("#".$x++,$_)} (@{$_->args})
	# 	    my $vm = InstAL::VarMapping->new(\@vrs);

	# 	    while($vm->hasMore){
	# 		my ($asp) = $_->genASP($vm);
	# 		push @rules,@{$asp};
	# 		$vm->nextMapping;
	# 	    }
	# 	}else{
	# 	    push @rules,@{$_->genASP()};
	# 	}
	#     }

	return \@rules;
}

sub timeCodes {
	my $self    = shift;
	my $nstates = shift;

	my @asp;

	for ( my $i = 0 ; $i < $nstates ; $i++ ) {
		push @asp,
		  ASP::Atom->new( 'instant',
			[ ASP::Atom->new( 'i' . sprintf( "%.2d", $i ) ) ] );
		push @asp,
		  ASP::Atom->new(
			'next',
			[
				ASP::Atom->new( 'i' . sprintf( "%.2d", $i ) ),
				ASP::Atom->new( 'i' . sprintf( "%.2d", $i + 1 ) )
			]
		  );

	}
	push @asp,
	  ASP::Atom->new( 'instant',
		[ ASP::Atom->new( 'i' . sprintf( "%.2d", $nstates ) ) ] );
	push @asp,
	  ASP::Atom->new( 'final',
		[ ASP::Atom->new( 'i' . sprintf( "%.2d", $nstates ) ) ] );
	return [ map { ASP::Rule->new($_) } (@asp) ];
}
1;
