package InstAL::Type::PermType;
use InstAL::VarMapping;
use InstAL::Ref::VarRef;
use InstAL::LiteralPool;

use vars qw/$DEBUG $AUTOLOAD/;
use strict;
use InstAL::CodeFragment;
use base 'InstAL::Type';


sub new {
    my $class = shift;
    my $event = shift;
    
    my $lp = InstAL::LiteralPool->lp;
    my $ft = $lp->get_types('Fluent');
    
    return bless {-name=>'PERM_'. $event->typeName, 
		  -literals=>[],
		  -super=>$ft
    }, $class;    

}

sub getType{ 	
    my $self = shift;
    return InstAL::LiteralPool->lp->permType;
}

1;
