package ASP::Comment;

use OCC::Resolver;

sub AUTOLOAD {
	return OCC::Resolver::resolve( __PACKAGE__, $AUTOLOAD, @_ );
}

sub new {
	my $class = shift;
	my $comment = shift || '';
	$body = [] if !$body;
	return bless {
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
		$str .= "%\n%".$comment ;
	}

	return $str;
}
1;
