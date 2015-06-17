package InstAL::Rule;
use OCC::Resolver;
use vars qw/$AUTOLOAD $DEBUG $vn/;
use strict;
use Carp qw/cluck/;
use InstAL::CodeFragment;
use base 'InstAL::CodeFragment';

$DEBUG=$ENV{IALDBG};
#$DEBUG = 1;
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

$vn =0;
sub newName{
    $vn++;
    return "RULE$vn";
    
}
# Looks up a varaiable reference in the lookup table and checks it
# against any existing references (if any) returns the correct varref.
sub add_varref{
    my $self = shift;
    my $varref = shift;
    my $target = shift;

    $self->error( "erm.." ) if !$varref;


    return $varref  if UNIVERSAL::isa($varref,'InstAL::Ref::LiteralRef');
    $self->error("not a varref") if(!UNIVERSAL::isa($varref,'InstAL::Ref::VarRef'));

    my $varname = $varref->var;
    if(!$self->get_varrefs($varname)){
	$self->set_varrefs($varname,[]);
    }


    my $ref =  $self->get_vars($varname);
    
    my $lp = InstAL::LiteralPool->lp;
    my $anytype = $lp->get_types('ANY');
    
    if($ref){
	print STDERR "Found existing use of  $varname, with type ",$ref->type->name ," attempting to refine type from ", $varref->type->name,"\n" if $DEBUG;

	if($ref->type->isSubType($varref->type)){
	    
	    print STDERR "Refining new  declaration of $varname:",$varref->type->name," to ", $ref->type->name,"\n" if $DEBUG;
	    $varref->set_type($ref->type);
	    
	}elsif($varref->type->isSubType($ref->type)){
	    print STDERR "Refining existing declaration of $varname:",$varref->type->name," to ", $varref->type->name,"\n" if $DEBUG;
	    
	    $ref->set_type($varref->type);
	}elsif($varref->type != $ref->type){
	    $target->error("Incompatible types for $varname: current is ",$varref->type ,":",$varref->type->name, ", previous was  ", $ref->type,":",$ref->type->name);
	}else{
	    
	    print STDERR "Not refining var $varname:",$varref->type->name," \n" if $DEBUG;
	    
	}
    }else{
	print "Added $varname:",$varref->type->name," to rule \n"if $DEBUG;
	$ref = $varref;
	$self->set_vars($varname,$varref);
    }
    

    
    die("Something aint right") if !$ref;
    push @{$self->get_varrefs($varname)},$target;
    return $ref;
}


# resolves assignments for the variables in this rule. 
sub resolveVars{
    die "Should never be called\n";
}

sub validMapping{
    my $self = shift;
    my $mapping= shift;
    return $self->cond->validMapping($mapping);
}

sub resolveUsedLiterals{
    die "Should never be called\n";
}

sub genASP{
    die "Should never be called\n;";   
}



1;
