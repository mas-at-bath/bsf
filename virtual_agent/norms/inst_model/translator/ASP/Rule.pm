package ASP::Rule;

use OCC::Resolver;

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class = shift;
	my $head  = shift;
	my $body  = shift;
	my $comment = shift || '';
	$body = [] if !$body;
	return bless {
		-head    => $head,
		-body    => $body,
		-comment => $comment
	}, $class;

}

#FIXME quick hack for comments
sub toString {
	my $self = shift;
	my $str;
	if ( $self->comment ) {
		my $comment = $self->comment;
		$comment =~ s/\n/\n% /gs;
		chomp $comment;
		$str .= "%".$comment."\n" ;
	}

	if($self->head){
		$str .= $self->head->tostring;
	}
	if ( @{ $self->body } ) {
		$str .= " :- \n\t" . join ",",
		  ( map { $_->tostring } @{ $self->body } );
		 
	}
	if($self->head || @{$self->body}){
		$str .= ".";
	}
	return $str;
}
1;
