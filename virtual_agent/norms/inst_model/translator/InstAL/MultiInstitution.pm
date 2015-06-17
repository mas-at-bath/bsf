package InstAL::MultiInstitution;
use InstAL::Institution;
 
use strict;
use OCC::Resolver;
use InstAL::Fluent;
use InstAL::Type;
use InstAL::VarMapping;
use InstAL::LiteralPool;
use ASP::Atom;
use ASP::Rule;

use base 'InstAL::Institution';
use vars qw/$AUTOLOAD $DEBUG/;
$DEBUG = $ENV{IALDBG};

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);

}


sub new {
    my $class = shift;
    my $lp = shift or die "No literal pool";
    my $self = bless {-name=>"none",
		      -flags=>[],
		      -symboltable=>{},
		      -initiallys=>[],
		      -genrules=>[],
		      -initrules=>[],
		      -termrules=>[],
		      -types=>{},
		      -facts=>{},
		      -events=>{},
		      -lp=>$lp
		      
    },$class;
    

    return $self;;
}

sub generateASP{
    my $self = shift;
    
    my @rules;
    
    my @allrules;
    my $lp = InstAL::LiteralPool->lp;


#    push @allrules,@{$self->initrules};
#    push @allrules,@{$self->termrules};
    push @allrules,@{$self->genrules};
#    push @allrules,@{$self->initiallys};

    foreach (@allrules){ print STDERR ">>Generating rule ",$_->name,":
	", $_->toString,"\n" if $DEBUG;

	my @vars = values(%{$_->vars});
	print STDERR "My vars ar [",join (",",map{$_->var.":".$_->type->name}(@vars)),"]\n" if $DEBUG;
	
	if(@vars){
	    my $vm = InstAL::VarMapping->new([values(%{$_->vars})],1);
	    print STDERR "&&&&& ", $vm->toString(),"\n";
	    while($vm->hasMore){
		my ($asp,$facts) = $_->genASP($vm,$self->lp);
		print STDERR "Expanding ",$vm->toString,"\n" if $DEBUG;
		push @rules,@{$asp};
	
		$vm->nextMapping;
	    }
	}else{
	    my ($asp,$facts) = $_->genASP();
	    push @rules,@{$asp};
	

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

1;
