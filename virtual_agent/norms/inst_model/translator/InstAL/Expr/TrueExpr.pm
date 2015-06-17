package InstAL::Expr::TrueExpr;
use InstAL::Expr;
use OCC::Resolver;

use vars qw /@ISA/;

@ISA = qw/InstAL::Expr/;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new{
    my $type = shift;
    return bless {},$type;
}
1;

sub toString{
    return "";
}

sub genASP{
    return [];
} 

sub resolveVars{
    
}

1;
