package InstAL::Literal;
use OCC::Resolver;
use strict;
use vars qw( $AUTOLOAD $DEBUG $parser);
use ASP::RDParser;

my $DEBUG=$ENV{IALDBG}	;

sub BEGIN{
    $parser = new ASP::RDParser();
}
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

=head1 new
=cut
sub new {
    my $class = shift;
    my $name = shift;
    my $type = shift;
    my $atom = shift;
    
    $atom = parseToAtom($name) if !$atom;
    return bless {-type=>$type,
		  -name=>$name,
		  -atom=>$atom
		  -used=>0
	      }, $class;
}

sub atom{
    my $self = shift;
    return $parser->atom($self->name);
}
sub parseToAtom{
    my $name = shift;
   
#HACK HACK HACK  should retain literal in atom form.
    return $parser->atom($name)
#HACK HACK HACK     
}

sub markUsed{
    my $self = shift;
    print STDERR "Marking ",$self->name," as used \n" if  !$self->{-used} && $DEBUG;
    $self->{-used} =1;
    $self->type->add_used_literal($self);
}

1;
