package InstAL::Type::PowType;
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
    
    return bless {-name=>'POW_'. $event->typeName, 
		  -literals=>[],
		  -super=>$ft
    }, $class;    

}

1;
