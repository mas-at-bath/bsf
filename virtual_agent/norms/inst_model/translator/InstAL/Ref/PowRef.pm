package InstAL::Ref::PowRef;
use InstAL::Ref::FluentRef;
use OCC::Resolver;
use InstAL::LiteralPool;
use vars qw /$AUTOLOAD/;
use strict;
use base 'InstAL::Ref::FluentRef';

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub is_static{
    my $self = shift;
    return 0;
}


sub getType{
    my $self = shift; 
    my $lp = InstAL::LiteralPool->lp;
    return $lp->typePow;
}

sub resolveVars{
    my $self = shift;
    $self->instref->resolveVars(@_);
    $self->evref->resolveVars(@_);
    if($self->evref->event->evtype ne InstAL::Event::INST()){
		$self->error("Illegal power: ", $self->evref->toString, " event ", $self->evref->event->name, " is not an inst event (\"",$self->evref->event->evtype,"\")");
    }    
}

sub new{
    my $type = shift;
    my $inst = shift;
    my $evref = shift;
    my $implicit = shift;


    return bless {-instref=>$inst,
		  -evref=>$evref,
		  -inst_implicit=>$implicit
    },$type;
}

sub innerASP{
    my $self = shift;
    my $mapping = shift;
    my $inst = $self->instref->innerASP($mapping);
    my $ia = $self->evref->innerASP($mapping);
    my $elit = InstAL::LiteralPool->lp()->get_literal($ia->tostring());
    #print "Elit is ",$elit->name, ": ",$elit->type,"\n";
    return ASP::Atom->new('pow',[$inst,$ia]);
}


sub fluent {
	my $self  = shift;
	my $event = $self->evref->event();
	my $fn    = $event->powFluentName();

	print STDERR "looking up fluent $fn type for ", $self->toString(), "\n";
	my $fluent = InstAL::LiteralPool->lp()->get_fluents($fn);
	if(!defined($fluent)){
		die "Unable to find pow fluent $fn ";
	}
	return $fluent;

}

sub toString{
    my $self = shift;
    
    if($self->is_inst_implicit){
	return "pow(".
	    $self->evref->toString.")";
	
    }else{
	return "pow(".$self->instref->toString.":".
	    $self->evref->toString.")";

    }
    
}

1;
