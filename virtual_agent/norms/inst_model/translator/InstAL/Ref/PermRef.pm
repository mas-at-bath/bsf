package InstAL::Ref::PermRef;
use InstAL::Ref::FluentRef;
use OCC::Resolver;
use ASP::Atom;
use vars qw /@ISA/;
use Data::Dumper;
use InstAL::LiteralPool;
@ISA = qw/InstAL::Ref::FluentRef/;

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub is_static {
	my $self = shift;
	return 0;
}

sub getType {
	my $self = shift;
	my $lp   = InstAL::LiteralPool->lp;
	return $lp->typePerm;
}

sub new {
	my $type  = shift;
	my $evref = shift;

	return bless { -evref => $evref }, $type;
}

sub resolveVars {
	my $self = shift;
	my $rule = shift;
	$self->evref->resolveVars($rule);

	$Data::Dumper::Maxdepth = 2;

	#    print "PR:",Dumper(),"\n";
	my $evref = $self->evref;
	if (
		!(
			   $evref->event->evtype eq InstAL::Event::INST()
			|| $evref->event->evtype eq InstAL::Event::EXO()
		)
	  )
	{
		$self->error(
			"Illegal: permission ",
			$self->evref->toString, " event ",
			$self->evref->event->name,
			" is not an inst or external event"
		);
	}
}

sub innerASP {
	my $self    = shift;
	my $mapping = shift;

	my $ia = $self->evref->innerASP($mapping);
	return ASP::Atom->new( 'perm', [$ia] );

}

sub isnt {
	my $self = shift;
	return $self->evref->event->inst();

}

sub fluent {
	my $self  = shift;
	my $event = $self->evref->event();
	my $fn    = $event->permFluentName();

	print STDERR "looking up fluent $fn type for ", $self->toString(), "\n";
	my $fluent = InstAL::LiteralPool->lp()->get_fluents($fn);
	if(!defined($fluent)){
		die "Unable to find perm fluent $fn ";
	}
	return $fluent;

}

sub toString {
	my $self = shift;

	return "perm(" . $self->evref->toString . ")";

}
1;
