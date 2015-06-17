package ASP::TreeWalker;

sub new {
    my $type = shift;
    return bless {},  $type;
}

sub apply{
    my $self = shift;
    my $before = shift;
    my $between = shift;
    my $after = shift;
    my $atom = shift;
    

    &$before($atom);
    my $first = 1;
    foreach my $arg ($atom->args){
	if(!$first){
	    &$between($atom);
	}
	$self->apply($before,$between,$after,$arg);
	$first = 0;
    }
    
    &$after($atom);
    
}

1;
