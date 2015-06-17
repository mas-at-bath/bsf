package InstAL::CodeFragment;

use OCC::Resolver;
sub AUTOLOAD{
    return OCC::Resolver::resolve(__PACKAGE__,$AUTOLOAD,@_);
}

sub new {
    my $class = shift;
    
    my $self  = bless {}, $class;
    $self->setLoc(@_);
    return $self;
}
sub setLoc{
    my $self = shift;
    my $file = shift; 
    my $line = shift; 
    my $col = shift;
    $self->{-loc_file} = $file;
    $self->{-loc_line} = $line;
    $self->{-loc_col} = $col;
    
}

sub loc_file{
    my $self = shift;
    return $self->{-loc_file};
}

sub loc_line{
    my $self = shift;
    return $self->{-loc_line};
}
sub loc_col{
    my $self = shift;
    return $self->{-loc_col};
}
sub copyLocFrom{
    my $self = shift;
    my $ref = shift;
    $self->{-loc_file} = $ref->loc_file;
    $self->{-loc_line} = $ref->loc_line;
    $self->{-loc_col} = $ref->loc_col;

}
sub getLocStr{
    my $self = shift;    
    return $self->loc_file .":".$self->loc_line. " col:". $self->loc_col;
}

sub getLoc{
    my $self = shift;
    return [$self->loc_file,$self->loc_line, $self->loc_col];

}

sub error{
    my $self = shift;
    my $msg =join("",@_);
    return InstAL::LiteralPool->lp->error($msg,$self);
}

sub warn{
    my $self = shift;
    my $msg =shift;
    return InstAL::LiteralPool->lp->warn($msg,$self);
}

1;
