package ASP::Literal;

#
sub new {
    my $class = shift;
    return bless {-head=>[],
		  -body=>[]},$class;
	
}

1;