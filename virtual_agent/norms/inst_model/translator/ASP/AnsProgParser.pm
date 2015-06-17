####################################################################
#
#    This file was generated using Parse::Yapp version 1.05.
#
#        Don't edit this file, use source file instead.
#
#             ANY CHANGE MADE HERE WILL BE LOST !
#
####################################################################
package ASP::AnsProgParser;
use vars qw ( @ISA );
use strict;

@ISA= qw ( Parse::Yapp::Driver );
use Parse::Yapp::Driver;

#line 2 "aspprog.yp"
 
    use vars qw/$DEBUG/;
    use ASP::Atom;
    use ASP::AnsProgram;
    use ASP::Rule;
    


sub new {
        my($class)=shift;
        ref($class)
    and $class=ref($class);

    my($self)=$class->SUPER::new( yyversion => '1.05',
                                  yystates =>
[
	{#State 0
		DEFAULT => -2,
		GOTOS => {
			'asprog' => 1,
			'rules' => 2
		}
	},
	{#State 1
		ACTIONS => {
			'' => 3
		}
	},
	{#State 2
		ACTIONS => {
			":-" => 5,
			'IDENT' => 7
		},
		DEFAULT => -1,
		GOTOS => {
			'head' => 4,
			'atom' => 6,
			'rule' => 8
		}
	},
	{#State 3
		DEFAULT => 0
	},
	{#State 4
		ACTIONS => {
			":-" => 9,
			"." => 10
		}
	},
	{#State 5
		ACTIONS => {
			'NOT' => 14,
			'IDENT' => 7
		},
		GOTOS => {
			'body' => 11,
			'body_atom' => 13,
			'atom' => 12
		}
	},
	{#State 6
		DEFAULT => -7
	},
	{#State 7
		ACTIONS => {
			'LB' => 15
		},
		DEFAULT => -13
	},
	{#State 8
		DEFAULT => -3
	},
	{#State 9
		ACTIONS => {
			'NOT' => 14,
			'IDENT' => 7
		},
		GOTOS => {
			'body' => 16,
			'body_atom' => 13,
			'atom' => 12
		}
	},
	{#State 10
		DEFAULT => -4
	},
	{#State 11
		ACTIONS => {
			'CO' => 17,
			"." => 18
		}
	},
	{#State 12
		DEFAULT => -10
	},
	{#State 13
		DEFAULT => -8
	},
	{#State 14
		ACTIONS => {
			'IDENT' => 7
		},
		GOTOS => {
			'atom' => 19
		}
	},
	{#State 15
		ACTIONS => {
			'VARIABLE' => 20,
			'IDENT' => 7
		},
		GOTOS => {
			'atom' => 21,
			'args' => 22,
			'term' => 23
		}
	},
	{#State 16
		ACTIONS => {
			'CO' => 17,
			"." => 24
		}
	},
	{#State 17
		ACTIONS => {
			'NOT' => 14,
			'IDENT' => 7
		},
		GOTOS => {
			'body_atom' => 25,
			'atom' => 12
		}
	},
	{#State 18
		DEFAULT => -6
	},
	{#State 19
		DEFAULT => -11
	},
	{#State 20
		DEFAULT => -17
	},
	{#State 21
		DEFAULT => -16
	},
	{#State 22
		ACTIONS => {
			'RB' => 27,
			'CO' => 26
		}
	},
	{#State 23
		DEFAULT => -14
	},
	{#State 24
		DEFAULT => -5
	},
	{#State 25
		DEFAULT => -9
	},
	{#State 26
		ACTIONS => {
			'VARIABLE' => 20,
			'IDENT' => 7
		},
		GOTOS => {
			'atom' => 21,
			'term' => 28
		}
	},
	{#State 27
		DEFAULT => -12
	},
	{#State 28
		DEFAULT => -15
	}
],
                                  yyrules  =>
[
	[#Rule 0
		 '$start', 2, undef
	],
	[#Rule 1
		 'asprog', 1, undef
	],
	[#Rule 2
		 'rules', 0,
sub
#line 11 "aspprog.yp"
{[]}
	],
	[#Rule 3
		 'rules', 2,
sub
#line 12 "aspprog.yp"
{push @{$_[1]},$_[2];$_[1]}
	],
	[#Rule 4
		 'rule', 2,
sub
#line 14 "aspprog.yp"
{new ASP::Rule($_[1])}
	],
	[#Rule 5
		 'rule', 4,
sub
#line 15 "aspprog.yp"
{new ASP::Rule($_[1],$_[3])}
	],
	[#Rule 6
		 'rule', 3,
sub
#line 16 "aspprog.yp"
{new ASP::Rule(undef,$_[2])}
	],
	[#Rule 7
		 'head', 1, undef
	],
	[#Rule 8
		 'body', 1,
sub
#line 19 "aspprog.yp"
{[$_[1]]}
	],
	[#Rule 9
		 'body', 3,
sub
#line 20 "aspprog.yp"
{push @{$_[1]},$_[3]; $_[1]}
	],
	[#Rule 10
		 'body_atom', 1, undef
	],
	[#Rule 11
		 'body_atom', 2,
sub
#line 23 "aspprog.yp"
{$_[2]->add_tag('NOT');$_[2]}
	],
	[#Rule 12
		 'atom', 4,
sub
#line 25 "aspprog.yp"
{$_[0]->resolve_atom(new ASP::Atom($_[1],$_[3]))}
	],
	[#Rule 13
		 'atom', 1,
sub
#line 26 "aspprog.yp"
{$_[0]->resolve_atom(new ASP::Atom($_[1],[]))}
	],
	[#Rule 14
		 'args', 1,
sub
#line 29 "aspprog.yp"
{[$_[1]]}
	],
	[#Rule 15
		 'args', 3,
sub
#line 30 "aspprog.yp"
{push @{$_[1]},$_[3]; $_[1]}
	],
	[#Rule 16
		 'term', 1, undef
	],
	[#Rule 17
		 'term', 1,
sub
#line 33 "aspprog.yp"
{my $a = new ASP::Atom($_[1],[]); $a->add_tag('VAR');$a;}
	]
],
                                  @_);
    bless($self,$class);
}

#line 36 "aspprog.yp"

    
    
sub resolve_atom{
	my $self  = shift; 
	my $atom = shift;
	my $key = $atom->tostring;
	if($self->{-atoms}->{$key}){
	    return $self->{-atoms}->{$key};
	}else{
	    $self->{-atoms}->{$key} = $atom;
	    return $atom;
	}
}

sub _Error {
    my $self = shift;
    my($value)=$self->YYCurval;
    
    my($what)= $self->YYCurtok;
    
    print("Unexpected \"$what\":\"$value\" expected one of ",join(",",$self->YYExpect),"\n");
    
    print "here:  ", $self->{-line},"\n";
    die "Syntax error.\n";
    
}


sub readline{
    my $self = shift;
    my $fh = $self->{-input};
    my $line =  $fh->getline;
    $self->{-line} = $line;
    $self->{-lineno}++;
    return $line;
}

sub _Lexer {
    my($self)=shift;

    while(1){
#	print "Lexer called ($fh)\n";
	$self->YYData->{INPUT}
	or  $self->YYData->{INPUT} = $self->readline
	    or  return('',undef);

	print "Lexing \"",$self->YYData->{INPUT},"\"\n" if $DEBUG;	
	$self->YYData->{INPUT} =~s/%.*//;
	chomp $self->YYData->{INPUT};

	my $prev = $self->YYData->{INPUT};
	my @tok = ();
	$self->YYData->{INPUT}=~s/^\s+//;

	if($self->YYData->{INPUT} =~s/^\(// ){
 	    @tok = ('LB','(');
	}elsif($self->YYData->{INPUT} =~ s/^not//){
	    @tok = ('NOT','not'); 
	}elsif($self->YYData->{INPUT} =~ s/^\:-//){
	    @tok = (':-',':-'); 
	}elsif($self->YYData->{INPUT} =~ s/^\.//){
	    @tok = ('.','.'); 
	}elsif($self->YYData->{INPUT} =~ s/^\)//){
	    @tok = ('RB',')'); 
	}elsif($self->YYData->{INPUT} =~s/^\,//){
	    @tok = ('CO',',');
	}elsif($self->YYData->{INPUT} =~s/^([a-z][a-zA-Z0-9_]*)//i){
	    @tok =  ('IDENT',$1);
	}elsif($self->YYData->{INPUT} =~s/^([A-Z][a-zA-Z0-9_]*)//i){
	    @tok =  ('VARIABLE',$1);
	}else{

	}
	print "Yeild ",$tok[0],":",$tok[1],"\n" if $DEBUG;
	return @tok if $tok[0];
	# there is data on the input and we haven't parsed it.
	if($prev && $prev eq $self->YYData->{INPUT}){
	    die "Unexpected token at $prev";
	}
	    
    }    
    
#    $parser->YYData->{INPUT}=~s/^[ \t]//;
#    print "Lex called ",$parser->YYData->{INPUT},"\n";
#    print "Called with\"", substr($parser->YYData->{INPUT},0,20),"\"\n";

}

sub parseASPProg {
    my $self = shift;
    my $input = shift;
    my $filename = shift;
    $self->{-lineno} = 0;
    $self->{-cline} = 0;
    $self->{-atoms} = {};
    $self->{-input} = $input;
    my $rules = $self->YYParse(yylex=>\&_Lexer,yyerror=>\&_Error);
#    print Dumper($atoms);
    return ASP::AnsProgram->new($rules);
}

1;
