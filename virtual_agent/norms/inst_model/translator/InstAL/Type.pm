package InstAL::Type;
use strict;
use InstAL::CodeFragment;
use base 'InstAL::CodeFragment';
use OCC::Resolver;
use vars qw/$DEBUG $AUTOLOAD/;

$DEBUG = $ENV{IALDBG};

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );

}

sub new {
	my $class    = shift;
	my $name     = shift;
	my $internal = shift;
	my $super    = shift;

	return bless {
		-name          => $name,
		-internal      => $internal,
		-literals      => [],
		-super         => $super,
		-used_literals => {},
		-subtypes      => {}
	}, $class;
}

sub getTypeDeps {
	my $self = shift;
	my @deps;
	if ( $self->super ) {
		return [ $self->super ];
	}
	return [];

}

sub getSubTypes {
	my $self     = shift;
	my @subtypes = ();
	foreach ( values %{ $self->subtypes } ) {
		push @subtypes, @{ $_->subtypes };
	}
	return \@subtypes;
}

# is this a subtype of that?
sub isSubType {
	my $self  = shift;
	my $ctype = shift;
	my $tsup;

	$tsup = $self->super;
	while ($tsup) {
		return 1 if $tsup == $ctype;
		$tsup = $tsup->super;
	}
	return 0;
}

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );

}

sub getAllUsedLiterals {
	my $self     = shift;
	my $literals = shift;
	$literals = {} if !$literals;
	print STDERR "Getting used literals for ", $self->typeName, "\n" if $DEBUG;

	foreach ( @{ $self->literals } ) {
		$literals->{$_} = $_ if $self->literal_is_used($_);
	}

	print STDERR "subtypes of  ", $self->typeName, " are ",
	  join( ",", map { $_->typeName } ( values %{ $self->subtypes } ) ), "\n"
	  if $DEBUG;
	foreach my $t ( values %{ $self->subtypes } ) {

		$t->getAllUsedLiterals($literals);

	}
	return [ values %{$literals} ];

}

sub getAllLiterals {
	my $self     = shift;
	my $literals = shift;
	$literals = {} if !$literals;    
	my $nlits = @{ $self->literals };
	print STDERR "Type>>Getting literals for ", $self->typeName,
	  " ($nlits here)\n"
	  if $DEBUG;
	foreach ( @{ $self->literals } ) {
		$literals->{$_} = $_;
	}

	print STDERR "subtypes of  ", $self->typeName, " are ",
	  join( ",", map { $_->typeName } ( values %{ $self->subtypes } ) ), "\n"
	  if $DEBUG;
	foreach my $t ( values %{ $self->subtypes } ) {

		$t->getAllLiterals($literals);
	}
	print STDERR "Type<<Literals of ", $self->typeName, " are :",
	  join( ",", values( %{$literals} ) ), "\n"
	  if $DEBUG;
	return [ values %{$literals} ];
}

sub typeName {
	my $self = shift;
	return $self->name;
}

sub toDefString {
	my $self = shift;
	return "" if $self->is_internal;
	return "type " . $self->toString . ";\n";

}

sub toString {
	my $self = shift;
	return $self->name;
}

sub add_used_literal {
	my $self    = shift;
	my $literal = shift;

	$self->{-used_literals} = {} if !$self->{-used_literals};

	$self->{-used_literals}->{$literal} = 1;

}

sub literal_is_used {
	my $self    = shift;
	my $literal = shift;
	return 0 if !$self->{-used_literals};
	return $self->{-used_literals}->{$literal};
}

sub dbgToString {
	my $self   = shift;
	my $indent = shift;
	my $str    = "";
	$str .= $indent . "-----------------\n";
	$str .= $indent . "===" . $self->typeName . "===\n";
	$str .= $indent . "Literals:\n";

	if ( !@{ $self->literals } ) {
		$str .= $indent . " NONE\n";
	}
	else {
		$str .= $indent . "Internal: "
		  . join(
			",",
			(
				map { "'" . $_->name . "'" }
				  ( sort { $a->name cmp $b->name } ( @{ $self->literals } ) )
			)
		  )
		  . "\n";
		$str .= $indent . "All     : "
		  . join(
			",",
			(
				map { "'" . $_->name . "'" } (
					sort { $a->name cmp $b->name }
					  ( @{ $self->getAllLiterals } )
				)
			)
		  )
		  . "\n";
		$str .= $indent . "Used    : "
		  . join(
			",",
			(
				map { "'" . $_->name . "'" } (
					sort { $a->name cmp $b->name } (
						grep { $_->type->literal_is_used($_) }
						  ( @{ $self->getAllLiterals } )
					)
				)
			)
		  )
		  . "\n";

	}

	my @subtypes =
	  sort { $a->typeName cmp $b->typeName } ( values %{ $self->subtypes } );
	if (@subtypes) {
		$str .= $indent . "SubTypes:\n";
		foreach (@subtypes) {
			$str .= $_->dbgToString( $indent . "   " );
		}
	}
	return $str;
}

1;

