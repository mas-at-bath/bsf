package ASP::AnsProgram;
use OCC::Resolver;
use vars qw/$AUTOLOAD/;
use strict;

sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}


sub new {
    my $class = shift;
    my $rules = shift;    
    my $self =bless {-rules=>$rules},$class;
    return  $self;
}




sub headMatchesName{
    my $self = shift;
    my $name = shift;
    return sub {
	my $rule = shift;
	$rule->head && $rule->head->name eq $name;
    };
}

sub bodyContainsAtom{
    my $self= shift;
    my $atom = shift;
    return sub {
	my $rule = shift;
	foreach (@{$rule->body}){
	    if( $_->cmp($atom)){
		return 1;
	    }
	}
	return 0;
    }
}
sub and {
    my $self = shift;
    my $exp1 = shift;
    my $exp2 = shift;
    return sub {
	my $rule = shift;
	&$exp1($rule) && &$exp2($rule);
    }
}

sub grep{
    my $self = shift;
    my $test = shift;
    my @rs;
    foreach (@{$self->rules}){
	if(&$test($_)){
	    push @rs ,$_;
	}
    }
    return @rs;
}
1;
