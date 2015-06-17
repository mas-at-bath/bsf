package InstAL::GeneratesRule;

use OCC::Resolver;
use InstAL::Rule;
use InstAL::Ref::LiteralRef;
use vars qw( @ISA  $AUTOLOAD $DEBUG) ;
use strict;
$DEBUG= $ENV{IALDBG};
@ISA = qw(InstAL::Rule);
use Data::Dumper;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

# types 0: initiates 1: termiates
sub new  {
    my $class = shift;
    my $event = shift; # the event which causes the transition
    my $gens = shift; # the generated events EventExprs
    my $cond = shift; # the condition expression an Expression or undef

    my $self = bless 
    {
	-event=>$event,#eventref
	-gens=>$gens,#[EventRefs]
	-cond=>$cond,#Expression
	-vars=>{}, # var=>type
	-varrefs=>{},  #var=>[target...]
	-name=>InstAL::Rule::newName()
    }, $class;


    return $self;
}



sub resolveVars{
    my $self =shift;
    print ">>Resolving head of " , $self->name, "\n" if $DEBUG;
    $self->event->resolveVars($self);
    print "<<Resolved head of " , $self->name, "\n" if $DEBUG;

    foreach (@{$self->gens}){
	$_->resolveVars($self);
    }
    $self->cond->resolveVars($self);
}

sub toString{
    my $self = shift;
    my $base = $self->event->toString;
    $base.= " generates ";
    $base.=join( ",",(map {$_->toString}(@{$self->gens})));
    
    if(ref($self->cond) ne 'InstAL::TrueExpr'){
	$base.= " if ". $self->cond->toString;
    }
    return $base.";\n";
	
}



sub resolveUsedLiterals{
    my $self = shift;
    my $lp = InstAL::LiteralPool->lp;
    
    if(values(%{$self->vars})){
	my $mapping = InstAL::VarMapping->new([values(%{$_->vars})]);
	while($mapping->hasMore){
	    if($self->cond->validMapping($mapping)){
		foreach(@{$self->gens}){
		    my $l = $_->resolveLiteral($mapping);
		    $l->markUsed;
		}
	    }
	    
	    $mapping->nextMapping;
	}
    }else{
	foreach(@{$self->gens}){
	    my $l = $_->resolveLiteral(0);
	    $l->markUsed;
	}
    }
}

sub genASP{
    my $self = shift;
    my $mapping = shift;
    my $lp = InstAL::LiteralPool->lp;
    die('badness') if not $lp;

    my ($valid,$expr) = $self->cond->validMapping($mapping);

    if(!$valid){
	print "Mapping ",$mapping->toString," is eliminated by expr ",$expr->toString,"\n" if $DEBUG;
	return ([],[]);
    }
    my @rules;
    foreach (@{$self->gens}){
	my $ev = $_->event($mapping,$lp);
	# HACK, with variables you can geneate obs events. 
	# HACK have to skip them for now
	if($ev->evtype eq InstAL::Event::EXO()){
	    print STDERR "Skipping generation of ",$ev->toString if $DEBUG;
	    next;
	}
	my ($head) = @{$_->genASP($mapping)};
	
	my @body;

	push @body,@{$self->event->genASP($mapping,InstAL::LiteralPool->lp->get_types('Event'))};

	push @body,@{$self->cond->genASP($mapping)};

	


	if($ev->evtype ne InstAL::Event::VIOL()){
	    my $trigger = $self->event->event($mapping,$lp);
	    my $inst = InstAL::Ref::LiteralRef->new($trigger->inst->varname,$lp);
	    print STDERR $ev->name." evtype is ",$ev->evtype,"\n" if $DEBUG;
	    my $powcond = InstAL::Ref::PowRef->new($inst,$_);
	
	    my $asp = $powcond->genASP($mapping);

	    push @body,@{$powcond->genASP($mapping)};
	}
	
	push @body, ASP::Atom->new('instant',[ASP::Atom->new('I')]);
#	print "ASP ",ASP::Rule->new(ASP::Atom->new("foo"),\@body)->toString,"\n";
	push @rules, ASP::Rule->new($head,\@body);
    }
       return \@rules;

}
