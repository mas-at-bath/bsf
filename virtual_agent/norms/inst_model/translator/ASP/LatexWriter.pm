package ASP::LatexWriter;

use ASP::TreeWalker;

use vars qw /%MAPPING/;

use strict;
%MAPPING = (
    obl=>['\obl{','}{','}'],
    perm=>['\perm{','}{','}'],
    pow=>['\pow{','}{','}'],
    viol=>['\viol{','}{','}']);


sub new{
    my $class = shift;
    my $stream = shift;    
    return bless {-stream=>$stream}, $class;
}


sub stream{
    my $self = shift;
    return $self->{-stream};
}

sub getHead{
    my $self = shift;
    my $atom = shift;
        
}



sub writeAtom{
    my $self = shift;
    my $atom = shift;
    my $treewalker = ASP::TreeWalker->new;
    
    my $before = sub {
	my $atom = shift;
	my $head;
	if($MAPPING{$atom->name}){
	    $head = $MAPPING{$atom->name}->[0] ;
	}else{
	    if($atom->args){
		$head = $atom->name."(";
	    }else{
		$head = $atom->name;
	    }
	}
	$self->stream->print($head);
	
    };
    my $between = sub{
	my $atom = shift;
	my $mid;
	
	if($MAPPING{$atom->name}){
	    $mid = $MAPPING{$atom->name}->[1] ;
	}else{
	    $mid = ",";
	}
	$self->stream->print($mid);
    };
    my $after = sub{
	my $atom = shift;
	my $tail;
	
	if($MAPPING{$atom->name}){
	    $tail = $MAPPING{$atom->name}->[2] ;
	}else{
	    if($atom->args){
		$tail = ")";
	    }else{
		$tail = "";
	    }

	}
	$self->stream->print($tail);
    };
    
    $treewalker->apply($before,$between,$after,$atom);
}


sub close{
    my $self = shift;
    $self->stream->close;
}
sub graphAnsSet{
    my $self = shift;
    my $as =  shift;
    my @instants = $as->instants;
    my $date = `date`;
    chomp $date;
    my $head = "%% Generated on $date by ansset2latex\n";
    $self->stream->print($head);
    $self->stream->print("\\begin{psmatrix}[rowsep=0.2,colsep=0]\n");
    
    $self->stream->print (join '&',(map {my $name = $_; 
					 $name =~s/(\d+)/_\{$1\}/;
					 "\\circlenode{$_}{\$$name\$}"} (@instants)),"\\\\\n");
    my $first = 1;

    my %prev;
    foreach my $inst (@instants){
	if(!$first){
	    $self->stream->print("\&\n");
	}
	
	my @atoms = ASP::Atom->sort($as->istate_atoms($inst));
	my $sname = $inst.'p';

	$self->stream->print("\\Rnode{$sname}{\n\t\\tiny\\ensuremath{\n\t\t\\begin{array}{l}\n");
	foreach (@atoms){
	    $self->stream->print("\t\t\t");
	    if(!$first &&!$prev{$_->arg(0)}){
		$self->stream->print("\\mathbf{");
	    }
	    $self->writeAtom($_->arg(0));
	    if(!$first &&!$prev{$_->arg(0)}){
		$self->stream->print("}");
	    }
	    $self->stream->print("\\\\\n");
	}
	$self->stream->print("\t\t\\end{array}\n\t}\n}\n");
	
	
	%prev = (); # used for difference
	foreach (@atoms){
	    $prev{$_->arg(0)} = 1;
	}
	if($first){
	    $first =0;
	}
    }

    foreach(@instants){
	my $sname = $_.'p';
	$self->stream->print("\\ncline{-}{$_}{$sname}\n");
    }

    for(my $i = 0; $i < (@instants -1);$i++){	

	my @obs =  ASP::Atom->sort($as->observed_atoms($instants[$i]));
	my @occ =  ASP::Atom->sort($as->occured_atoms($instants[$i]));
	$self->stream->print("\\ncline{-}{",$instants[$i],"}{",$instants[$i+1],"}\n");
	
	$self->stream->print("\\aput{:U}{\\ensuremath{\\tiny\n\t\\begin{array}{c}\n");
	
	foreach(@obs){
	    $self->stream->print("\t\t{\\mathbf {");
	    $self->writeAtom($_->arg(0));
	    $self->stream->print("}}\\\\\n");
	}
	
	foreach(@occ){
	    $self->stream->print ("\t\t");
	    $self->writeAtom($_->arg(0));
	    $self->stream->print("\\\\\n");
	}
	
	$self->stream->print("\t\\end{array}}\n}\n");
    }
    
    $self->stream->print( "\\end{psmatrix}\n");

}
