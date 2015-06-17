package InstAL::Ref::EventVarRef;

use OCC::Resolver;
use ASP::Atom;
use Carp qw/cluck/;
use strict;
use vars  qw/@ISA $AUTOLOAD/;
use InstAL::Ref::VarRef;


use base 'InstAL::Ref::VarRef';


sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

# bit of a hack to get event variables working

sub new{
    my $class = shift;
    my $var = shift;
    my $type = shift;

    my $self = {
	-var=>$var,
	-type=>$type
    };
 
    return bless $self,$class;
}


sub resolveVars{
    my $self = shift;
    my $rule = shift;
    $rule->add_varref($self,$rule);   
}
sub toString{
    my $self = shift;
    return $self->var;
}


sub getType{
    my $self = shift;
    return $self->type;
}

#this is a stupid HACK HACK HACK
sub event{
    my $self = shift;
    my $mapping = shift;
    my $lp = InstAL::LiteralPool->lp();
    
    return $self if !$mapping;
    my $var = $self->var;    
    my $evfull = $mapping->getVar($var);
    my $evname;
    if($evfull =~/^viol\(([a-z0-9A-Z]+).*\)$/){
	$evname = "_viol_$1";
    }else{
	$evfull =~ /^([_a-zA-Z_0-9]+)[^a-zA-Z_0-9]*/;
	$evname = $1;
    }
    
#    print "Resolved event name of $evfull to $evname\n";
    $lp->get_events($evname) or die "sheeh, $evname not found in $lp:".$lp->toString;
    return $lp->get_events($evname);
    
}

sub args{
    my $self = shift;
    return [];
}

sub name{
    my $self = shift;
    return "NOTANEVENTYET";
}

sub permFluentName{
	return 'perm';
}

sub powFluentName{
	return 'pow';
}
sub genASP{
    my $self = shift;
    my $mapping = shift;
    my $expecting = shift;
    
    my $ia = $self->innerASP($mapping);
    return [ASP::Atom->new('occured',[$ia,ASP::Atom->new('I')])];
    
}

sub evtype{
    my $self = shift;
    return InstAL::Event::INST();
}
1;
