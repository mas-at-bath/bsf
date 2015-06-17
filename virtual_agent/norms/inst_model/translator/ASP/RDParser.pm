#!/usr/bin/perl 
package ASP::RDParser;
use ASP::AnswerSet;
use ASP::Atom;
use ASP::Parser;
use strict;
use Parse::RecDescent;
use Data::Dumper;
our @ISA = qw/Parse::RecDescent ASP::Parser/;

our $DEBUG=0;

# parses an smodels output models line into a list of atoms 
sub parseSmodels{
    my ($self,$code) = @_;

    my @atoms;

    print STDERR "Parsing code\n" if $DEBUG;
    my @split = split /\s/, $code;
    while(my $s = shift @split){
  	my $obj = $self->atom($s);
 	if(!$obj){
 	    print "parsing of $_ failed\n";
 	}else{
 	    push @atoms, $obj;
 	}
     }
#   print @{$self->ansset($code)},"Done\n";

#    my $tlatoms = $self->ansset($code);
    
    print STDERR "Parsed ", my $i = @atoms, " atoms\n" if $DEBUG;
    return  new ASP::AnswerSet(\@atoms,ASP::Parser::atompool());
    
}

my $gramar =q { 

ident: name 
ansset: atom(s) eofile {$item[1] }

atom:  ident '(' arguments ')' { ASP::RDParser::resolve_atom(new ASP::Atom($item{ident},$item{arguments}))}   | 
       ident ...!'('  { ASP::RDParser::resolve_atom(new ASP::Atom($item{ident}))} 

name: /[a-zA-Z0-9_]+/i  


arguments: atom ',' arguments {[$item[1], @{$item[3]}]} 
  |  atom { [$item[1]] }

trace: atom(s /\s/)

eofile: /^\Z/
  
};
   
sub new {
    my $class = shift;
    my $self = new Parse::RecDescent($gramar) or die @$;
    return bless $self, $class;
}    

sub resolve_atom{
    return shift;
}

sub test{
    my $parse = new ASPPARSER() or die @$;
    
    my @tests = ( "foo", "state(0,1,2)", "state(2,3,4)");
    
    
    foreach (@tests ){   
	my $obj = $parse->atom($_);
	if(!$obj){
	    print "parsing of $_ failed\n";
	}else{
	    print $_ , ":" ,"\n",Dumper($obj),"\n---------------------\n";
	}
    }
}
1;
