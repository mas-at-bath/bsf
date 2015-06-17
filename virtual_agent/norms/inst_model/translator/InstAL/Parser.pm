####################################################################
#
#    This file was generated using Parse::Yapp version 1.05.
#
#        Don't edit this file, use source file instead.
#
#             ANY CHANGE MADE HERE WILL BE LOST !
#
####################################################################
package InstAL::Parser;
use vars qw ( @ISA );
use strict;

@ISA= qw ( Parse::Yapp::Driver );
#Included Parse/Yapp/Driver.pm file----------------------------------------
{
#
# Module Parse::Yapp::Driver
#
# This module is part of the Parse::Yapp package available on your
# nearest CPAN
#
# Any use of this module in a standalone parser make the included
# text under the same copyright as the Parse::Yapp module itself.
#
# This notice should remain unchanged.
#
# (c) Copyright 1998-2001 Francois Desarmenien, all rights reserved.
# (see the pod text in Parse::Yapp module for use and distribution rights)
#

package Parse::Yapp::Driver;

require 5.004;

use strict;

use vars qw ( $VERSION $COMPATIBLE $FILENAME );

$VERSION = '1.05';
$COMPATIBLE = '0.07';
$FILENAME=__FILE__;

use Carp;

#Known parameters, all starting with YY (leading YY will be discarded)
my(%params)=(YYLEX => 'CODE', 'YYERROR' => 'CODE', YYVERSION => '',
			 YYRULES => 'ARRAY', YYSTATES => 'ARRAY', YYDEBUG => '');
#Mandatory parameters
my(@params)=('LEX','RULES','STATES');

sub new {
    my($class)=shift;
	my($errst,$nberr,$token,$value,$check,$dotpos);
    my($self)={ ERROR => \&_Error,
				ERRST => \$errst,
                NBERR => \$nberr,
				TOKEN => \$token,
				VALUE => \$value,
				DOTPOS => \$dotpos,
				STACK => [],
				DEBUG => 0,
				CHECK => \$check };

	_CheckParams( [], \%params, \@_, $self );

		exists($$self{VERSION})
	and	$$self{VERSION} < $COMPATIBLE
	and	croak "Yapp driver version $VERSION ".
			  "incompatible with version $$self{VERSION}:\n".
			  "Please recompile parser module.";

        ref($class)
    and $class=ref($class);

    bless($self,$class);
}

sub YYParse {
    my($self)=shift;
    my($retval);

	_CheckParams( \@params, \%params, \@_, $self );

	if($$self{DEBUG}) {
		_DBLoad();
		$retval = eval '$self->_DBParse()';#Do not create stab entry on compile
        $@ and die $@;
	}
	else {
		$retval = $self->_Parse();
	}
    $retval
}

sub YYData {
	my($self)=shift;

		exists($$self{USER})
	or	$$self{USER}={};

	$$self{USER};
	
}

sub YYErrok {
	my($self)=shift;

	${$$self{ERRST}}=0;
    undef;
}

sub YYNberr {
	my($self)=shift;

	${$$self{NBERR}};
}

sub YYRecovering {
	my($self)=shift;

	${$$self{ERRST}} != 0;
}

sub YYAbort {
	my($self)=shift;

	${$$self{CHECK}}='ABORT';
    undef;
}

sub YYAccept {
	my($self)=shift;

	${$$self{CHECK}}='ACCEPT';
    undef;
}

sub YYError {
	my($self)=shift;

	${$$self{CHECK}}='ERROR';
    undef;
}

sub YYSemval {
	my($self)=shift;
	my($index)= $_[0] - ${$$self{DOTPOS}} - 1;

		$index < 0
	and	-$index <= @{$$self{STACK}}
	and	return $$self{STACK}[$index][1];

	undef;	#Invalid index
}

sub YYCurtok {
	my($self)=shift;

        @_
    and ${$$self{TOKEN}}=$_[0];
    ${$$self{TOKEN}};
}

sub YYCurval {
	my($self)=shift;

        @_
    and ${$$self{VALUE}}=$_[0];
    ${$$self{VALUE}};
}

sub YYExpect {
    my($self)=shift;

    keys %{$self->{STATES}[$self->{STACK}[-1][0]]{ACTIONS}}
}

sub YYLexer {
    my($self)=shift;

	$$self{LEX};
}


#################
# Private stuff #
#################


sub _CheckParams {
	my($mandatory,$checklist,$inarray,$outhash)=@_;
	my($prm,$value);
	my($prmlst)={};

	while(($prm,$value)=splice(@$inarray,0,2)) {
        $prm=uc($prm);
			exists($$checklist{$prm})
		or	croak("Unknow parameter '$prm'");
			ref($value) eq $$checklist{$prm}
		or	croak("Invalid value for parameter '$prm'");
        $prm=unpack('@2A*',$prm);
		$$outhash{$prm}=$value;
	}
	for (@$mandatory) {
			exists($$outhash{$_})
		or	croak("Missing mandatory parameter '".lc($_)."'");
	}
}

sub _Error {
	print "Parse error.\n";
}

sub _DBLoad {
	{
		no strict 'refs';

			exists(${__PACKAGE__.'::'}{_DBParse})#Already loaded ?
		and	return;
	}
	my($fname)=__FILE__;
	my(@drv);
	open(DRV,"<$fname") or die "Report this as a BUG: Cannot open $fname";
	while(<DRV>) {
                	/^\s*sub\s+_Parse\s*{\s*$/ .. /^\s*}\s*#\s*_Parse\s*$/
        	and     do {
                	s/^#DBG>//;
                	push(@drv,$_);
        	}
	}
	close(DRV);

	$drv[0]=~s/_P/_DBP/;
	eval join('',@drv);
}

#Note that for loading debugging version of the driver,
#this file will be parsed from 'sub _Parse' up to '}#_Parse' inclusive.
#So, DO NOT remove comment at end of sub !!!
sub _Parse {
    my($self)=shift;

	my($rules,$states,$lex,$error)
     = @$self{ 'RULES', 'STATES', 'LEX', 'ERROR' };
	my($errstatus,$nberror,$token,$value,$stack,$check,$dotpos)
     = @$self{ 'ERRST', 'NBERR', 'TOKEN', 'VALUE', 'STACK', 'CHECK', 'DOTPOS' };

#DBG>	my($debug)=$$self{DEBUG};
#DBG>	my($dbgerror)=0;

#DBG>	my($ShowCurToken) = sub {
#DBG>		my($tok)='>';
#DBG>		for (split('',$$token)) {
#DBG>			$tok.=		(ord($_) < 32 or ord($_) > 126)
#DBG>					?	sprintf('<%02X>',ord($_))
#DBG>					:	$_;
#DBG>		}
#DBG>		$tok.='<';
#DBG>	};

	$$errstatus=0;
	$$nberror=0;
	($$token,$$value)=(undef,undef);
	@$stack=( [ 0, undef ] );
	$$check='';

    while(1) {
        my($actions,$act,$stateno);

        $stateno=$$stack[-1][0];
        $actions=$$states[$stateno];

#DBG>	print STDERR ('-' x 40),"\n";
#DBG>		$debug & 0x2
#DBG>	and	print STDERR "In state $stateno:\n";
#DBG>		$debug & 0x08
#DBG>	and	print STDERR "Stack:[".
#DBG>					 join(',',map { $$_[0] } @$stack).
#DBG>					 "]\n";


        if  (exists($$actions{ACTIONS})) {

				defined($$token)
            or	do {
				($$token,$$value)=&$lex($self);
#DBG>				$debug & 0x01
#DBG>			and	print STDERR "Need token. Got ".&$ShowCurToken."\n";
			};

            $act=   exists($$actions{ACTIONS}{$$token})
                    ?   $$actions{ACTIONS}{$$token}
                    :   exists($$actions{DEFAULT})
                        ?   $$actions{DEFAULT}
                        :   undef;
        }
        else {
            $act=$$actions{DEFAULT};
#DBG>			$debug & 0x01
#DBG>		and	print STDERR "Don't need token.\n";
        }

            defined($act)
        and do {

                $act > 0
            and do {        #shift

#DBG>				$debug & 0x04
#DBG>			and	print STDERR "Shift and go to state $act.\n";

					$$errstatus
				and	do {
					--$$errstatus;

#DBG>					$debug & 0x10
#DBG>				and	$dbgerror
#DBG>				and	$$errstatus == 0
#DBG>				and	do {
#DBG>					print STDERR "**End of Error recovery.\n";
#DBG>					$dbgerror=0;
#DBG>				};
				};


                push(@$stack,[ $act, $$value ]);

					$$token ne ''	#Don't eat the eof
				and	$$token=$$value=undef;
                next;
            };

            #reduce
            my($lhs,$len,$code,@sempar,$semval);
            ($lhs,$len,$code)=@{$$rules[-$act]};

#DBG>			$debug & 0x04
#DBG>		and	$act
#DBG>		and	print STDERR "Reduce using rule ".-$act." ($lhs,$len): ";

                $act
            or  $self->YYAccept();

            $$dotpos=$len;

                unpack('A1',$lhs) eq '@'    #In line rule
            and do {
                    $lhs =~ /^\@[0-9]+\-([0-9]+)$/
                or  die "In line rule name '$lhs' ill formed: ".
                        "report it as a BUG.\n";
                $$dotpos = $1;
            };

            @sempar =       $$dotpos
                        ?   map { $$_[1] } @$stack[ -$$dotpos .. -1 ]
                        :   ();

            $semval = $code ? &$code( $self, @sempar )
                            : @sempar ? $sempar[0] : undef;

            splice(@$stack,-$len,$len);

                $$check eq 'ACCEPT'
            and do {

#DBG>			$debug & 0x04
#DBG>		and	print STDERR "Accept.\n";

				return($semval);
			};

                $$check eq 'ABORT'
            and	do {

#DBG>			$debug & 0x04
#DBG>		and	print STDERR "Abort.\n";

				return(undef);

			};

#DBG>			$debug & 0x04
#DBG>		and	print STDERR "Back to state $$stack[-1][0], then ";

                $$check eq 'ERROR'
            or  do {
#DBG>				$debug & 0x04
#DBG>			and	print STDERR 
#DBG>				    "go to state $$states[$$stack[-1][0]]{GOTOS}{$lhs}.\n";

#DBG>				$debug & 0x10
#DBG>			and	$dbgerror
#DBG>			and	$$errstatus == 0
#DBG>			and	do {
#DBG>				print STDERR "**End of Error recovery.\n";
#DBG>				$dbgerror=0;
#DBG>			};

			    push(@$stack,
                     [ $$states[$$stack[-1][0]]{GOTOS}{$lhs}, $semval ]);
                $$check='';
                next;
            };

#DBG>			$debug & 0x04
#DBG>		and	print STDERR "Forced Error recovery.\n";

            $$check='';

        };

        #Error
            $$errstatus
        or   do {

            $$errstatus = 1;
            &$error($self);
                $$errstatus # if 0, then YYErrok has been called
            or  next;       # so continue parsing

#DBG>			$debug & 0x10
#DBG>		and	do {
#DBG>			print STDERR "**Entering Error recovery.\n";
#DBG>			++$dbgerror;
#DBG>		};

            ++$$nberror;

        };

			$$errstatus == 3	#The next token is not valid: discard it
		and	do {
				$$token eq ''	# End of input: no hope
			and	do {
#DBG>				$debug & 0x10
#DBG>			and	print STDERR "**At eof: aborting.\n";
				return(undef);
			};

#DBG>			$debug & 0x10
#DBG>		and	print STDERR "**Dicard invalid token ".&$ShowCurToken.".\n";

			$$token=$$value=undef;
		};

        $$errstatus=3;

		while(	  @$stack
			  and (		not exists($$states[$$stack[-1][0]]{ACTIONS})
			        or  not exists($$states[$$stack[-1][0]]{ACTIONS}{error})
					or	$$states[$$stack[-1][0]]{ACTIONS}{error} <= 0)) {

#DBG>			$debug & 0x10
#DBG>		and	print STDERR "**Pop state $$stack[-1][0].\n";

			pop(@$stack);
		}

			@$stack
		or	do {

#DBG>			$debug & 0x10
#DBG>		and	print STDERR "**No state left on stack: aborting.\n";

			return(undef);
		};

		#shift the error token

#DBG>			$debug & 0x10
#DBG>		and	print STDERR "**Shift \$error token and go to state ".
#DBG>						 $$states[$$stack[-1][0]]{ACTIONS}{error}.
#DBG>						 ".\n";

		push(@$stack, [ $$states[$$stack[-1][0]]{ACTIONS}{error}, undef ]);

    }

    #never reached
	croak("Error in driver logic. Please, report it as a BUG");

}#_Parse
#DO NOT remove comment

1;

}
#End of include--------------------------------------------------


#line 2 "instal.yp"

	use Data::Dumper;
	  use InstAL::Type;
	  use InstAL::Expr::AndExpr;
	  use InstAL::Expr::EqExpr;
	  use InstAL::Expr::FluentExpr;
	  use InstAL::Expr::NeExpr;
	  use InstAL::Expr::NotExpr;
	  use InstAL::Expr::TrueExpr;

	  use InstAL::Ref::EventRef;
	  use InstAL::Ref::EventVarRef;
	  use InstAL::Ref::FluentRef;
	  use InstAL::Ref::LiteralRef;
	  use InstAL::Ref::VarLiteralRef;
	  use InstAL::Ref::OblRef;
	  use InstAL::Ref::PermRef;
	  use InstAL::Ref::PowRef;
	  use InstAL::Ref::VarRef;
	  use InstAL::Ref::ViolEventRef;

	  use InstAL::Atom;
	  use InstAL::CausalRule;
	  use InstAL::DynamicRule;
	  use InstAL::Event;
	  use InstAL::Fluent;
	  use InstAL::ViolEvent;
	  use InstAL::GeneratesRule;
	  use InstAL::MultiGeneratesRule;
	  use InstAL::InitialRule;
	  use InstAL::Institution;

	  use vars qw/$DEBUG/;

	  $DEBUG = $ENV{IALDBGPARSE};


sub new {
        my($class)=shift;
        ref($class)
    and $class=ref($class);

    my($self)=$class->SUPER::new( yyversion => '1.05',
                                  yystates =>
[
	{#State 0
		ACTIONS => {
			'QUERY' => 1,
			'INSTITUTION' => 9,
			'DOMAIN' => 10,
			'MULTI' => 5
		},
		GOTOS => {
			'top' => 8,
			'query' => 2,
			'name' => 4,
			'mname' => 3,
			'multi' => 11,
			'instal' => 7,
			'grounding' => 6
		}
	},
	{#State 1
		ACTIONS => {
			":" => 12
		}
	},
	{#State 2
		DEFAULT => -4
	},
	{#State 3
		DEFAULT => -37,
		GOTOS => {
			'mevents' => 13
		}
	},
	{#State 4
		DEFAULT => -22,
		GOTOS => {
			'iflags' => 14
		}
	},
	{#State 5
		ACTIONS => {
			'VARNAME' => 15,
			'ATOMNAME' => 17
		},
		GOTOS => {
			'varg' => 16
		}
	},
	{#State 6
		DEFAULT => -5
	},
	{#State 7
		DEFAULT => -1
	},
	{#State 8
		ACTIONS => {
			'' => 18
		}
	},
	{#State 9
		ACTIONS => {
			'VARNAME' => 15,
			'ATOMNAME' => 17
		},
		GOTOS => {
			'varg' => 19
		}
	},
	{#State 10
		ACTIONS => {
			";" => 20
		}
	},
	{#State 11
		DEFAULT => -2
	},
	{#State 12
		ACTIONS => {
			'EXISTS' => 22,
			'NEVER' => 21,
			'FORALL' => 27
		},
		GOTOS => {
			'never_expr' => 25,
			'qexpr' => 23,
			'forall_expr' => 26,
			'exists_expr' => 24
		}
	},
	{#State 13
		ACTIONS => {
			'DEST' => 30,
			'INST' => 31,
			'VIOLATION' => 32,
			'EXOGENOUS' => 34,
			'CREATE' => 35
		},
		DEFAULT => -62,
		GOTOS => {
			'evtype' => 28,
			'mrules' => 29,
			'meventdecl' => 33
		}
	},
	{#State 14
		ACTIONS => {
			'DEFAULT' => 37
		},
		DEFAULT => -28,
		GOTOS => {
			'default_rule' => 36,
			'typedefs' => 39,
			'iflag' => 38
		}
	},
	{#State 15
		DEFAULT => -84
	},
	{#State 16
		ACTIONS => {
			";" => 40
		}
	},
	{#State 17
		DEFAULT => -83
	},
	{#State 18
		DEFAULT => 0
	},
	{#State 19
		ACTIONS => {
			";" => 41
		}
	},
	{#State 20
		DEFAULT => -14
	},
	{#State 21
		ACTIONS => {
			'LC' => 42
		}
	},
	{#State 22
		ACTIONS => {
			'LC' => 43
		}
	},
	{#State 23
		ACTIONS => {
			";" => 44
		}
	},
	{#State 24
		DEFAULT => -9
	},
	{#State 25
		DEFAULT => -10
	},
	{#State 26
		DEFAULT => -8
	},
	{#State 27
		ACTIONS => {
			'LB' => 45
		}
	},
	{#State 28
		ACTIONS => {
			'EVENT' => 46
		}
	},
	{#State 29
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		DEFAULT => -6,
		GOTOS => {
			'evprop' => 47,
			'mrule' => 51
		}
	},
	{#State 30
		DEFAULT => -57
	},
	{#State 31
		DEFAULT => -54
	},
	{#State 32
		DEFAULT => -55
	},
	{#State 33
		DEFAULT => -38
	},
	{#State 34
		DEFAULT => -53
	},
	{#State 35
		DEFAULT => -56
	},
	{#State 36
		DEFAULT => -24
	},
	{#State 37
		ACTIONS => {
			'PERMIT' => 53,
			'FORBID' => 54
		},
		GOTOS => {
			'defl_arg' => 52
		}
	},
	{#State 38
		DEFAULT => -23
	},
	{#State 39
		ACTIONS => {
			'TYPE' => 55
		},
		DEFAULT => -33,
		GOTOS => {
			'typedef' => 57,
			'decls' => 56
		}
	},
	{#State 40
		DEFAULT => -15
	},
	{#State 41
		DEFAULT => -16
	},
	{#State 42
		ACTIONS => {
			'queryexpr' => 58
		}
	},
	{#State 43
		ACTIONS => {
			'queryexpr' => 59
		}
	},
	{#State 44
		DEFAULT => -7
	},
	{#State 45
		ACTIONS => {
			'VARNAME' => 60
		}
	},
	{#State 46
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprop' => 61
		}
	},
	{#State 47
		ACTIONS => {
			'GENERATES' => 63
		}
	},
	{#State 48
		DEFAULT => -80
	},
	{#State 49
		ACTIONS => {
			'LB' => 64
		}
	},
	{#State 50
		ACTIONS => {
			'LB' => 65
		},
		DEFAULT => -78
	},
	{#State 51
		DEFAULT => -63
	},
	{#State 52
		ACTIONS => {
			";" => 66
		}
	},
	{#State 53
		DEFAULT => -27
	},
	{#State 54
		DEFAULT => -26
	},
	{#State 55
		ACTIONS => {
			'VARNAME' => 67
		}
	},
	{#State 56
		ACTIONS => {
			'DEST' => 30,
			'INST' => 31,
			'VIOLATION' => 32,
			'EXOGENOUS' => 34,
			'FLUENT' => 70,
			'CREATE' => 35,
			'STATIC' => 72,
			'NONINERTIAL' => 73
		},
		DEFAULT => -58,
		GOTOS => {
			'evtype' => 68,
			'decl' => 71,
			'eventdecl' => 74,
			'rules' => 69,
			'fluentdecl' => 75
		}
	},
	{#State 57
		DEFAULT => -29
	},
	{#State 58
		ACTIONS => {
			'RC' => 76
		}
	},
	{#State 59
		ACTIONS => {
			'RC' => 77
		}
	},
	{#State 60
		ACTIONS => {
			'RB' => 78
		}
	},
	{#State 61
		ACTIONS => {
			'SC' => 79
		},
		DEFAULT => -51,
		GOTOS => {
			'event_tail' => 80
		}
	},
	{#State 62
		ACTIONS => {
			'LB' => 81
		},
		DEFAULT => -17
	},
	{#State 63
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 82,
			'evproplist' => 83
		}
	},
	{#State 64
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 84
		}
	},
	{#State 65
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvargs' => 86,
			'openvarg' => 88
		}
	},
	{#State 66
		DEFAULT => -25
	},
	{#State 67
		ACTIONS => {
			'SC' => 89
		},
		DEFAULT => -31,
		GOTOS => {
			'super_typedef' => 90
		}
	},
	{#State 68
		ACTIONS => {
			'EVENT' => 91
		}
	},
	{#State 69
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50,
			'ALWAYS' => 92,
			'INITIALLY' => 95
		},
		DEFAULT => -3,
		GOTOS => {
			'rule' => 94,
			'evprop' => 93
		}
	},
	{#State 70
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprops' => 96,
			'typeprop' => 97
		}
	},
	{#State 71
		DEFAULT => -34
	},
	{#State 72
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprops' => 98,
			'typeprop' => 97
		}
	},
	{#State 73
		ACTIONS => {
			'FLUENT' => 99
		}
	},
	{#State 74
		DEFAULT => -36
	},
	{#State 75
		DEFAULT => -35
	},
	{#State 76
		DEFAULT => -12
	},
	{#State 77
		DEFAULT => -11
	},
	{#State 78
		ACTIONS => {
			'LC' => 100
		}
	},
	{#State 79
		ACTIONS => {
			'VARNAME' => 101
		}
	},
	{#State 80
		ACTIONS => {
			";" => 102
		}
	},
	{#State 81
		ACTIONS => {
			'VARNAME' => 103
		},
		GOTOS => {
			'typeargs' => 104,
			'type' => 105
		}
	},
	{#State 82
		DEFAULT => -75
	},
	{#State 83
		ACTIONS => {
			'CO' => 106,
			'IF' => 107
		},
		DEFAULT => -73,
		GOTOS => {
			'rule_tail' => 108
		}
	},
	{#State 84
		ACTIONS => {
			'RB' => 109
		}
	},
	{#State 85
		DEFAULT => -99
	},
	{#State 86
		ACTIONS => {
			'RB' => 111,
			'CO' => 110
		}
	},
	{#State 87
		ACTIONS => {
			'LB' => 112
		},
		DEFAULT => -97
	},
	{#State 88
		DEFAULT => -95
	},
	{#State 89
		ACTIONS => {
			'VARNAME' => 113
		}
	},
	{#State 90
		ACTIONS => {
			";" => 114
		}
	},
	{#State 91
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprop' => 115
		}
	},
	{#State 92
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 116,
			'fluentproplist' => 120
		}
	},
	{#State 93
		ACTIONS => {
			'INITIATES' => 123,
			'GENERATES' => 124,
			'TERMINATES' => 125
		},
		GOTOS => {
			'causal_type' => 122
		}
	},
	{#State 94
		DEFAULT => -59
	},
	{#State 95
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 116,
			'fluentproplist' => 126
		}
	},
	{#State 96
		ACTIONS => {
			";" => 127
		}
	},
	{#State 97
		ACTIONS => {
			'CO' => 128
		},
		DEFAULT => -42
	},
	{#State 98
		ACTIONS => {
			";" => 129
		}
	},
	{#State 99
		ACTIONS => {
			'PERM' => 133,
			'ATOMNAME' => 62,
			'POW' => 131
		},
		GOTOS => {
			'etypeprops' => 130,
			'typeprop' => 134,
			'etypeprop' => 132
		}
	},
	{#State 100
		ACTIONS => {
			'queryexpr' => 135
		}
	},
	{#State 101
		DEFAULT => -52
	},
	{#State 102
		DEFAULT => -49
	},
	{#State 103
		DEFAULT => -21
	},
	{#State 104
		ACTIONS => {
			'RB' => 137,
			'CO' => 136
		}
	},
	{#State 105
		DEFAULT => -19
	},
	{#State 106
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 138
		}
	},
	{#State 107
		ACTIONS => {
			'EQ' => 140,
			'NOT' => 141,
			'OBL' => 117,
			'POW' => 118,
			'PERM' => 119,
			'VARNAME' => 85,
			'ATOMNAME' => 147,
			'NE' => 148
		},
		GOTOS => {
			'fluentprop' => 139,
			'expr' => 146,
			'ne_op' => 145,
			'not_expr' => 144,
			'eq_op' => 143,
			'fluentexpr' => 142,
			'exprlist' => 149,
			'openvarg' => 150
		}
	},
	{#State 108
		ACTIONS => {
			";" => 151
		}
	},
	{#State 109
		DEFAULT => -77
	},
	{#State 110
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 152
		}
	},
	{#State 111
		DEFAULT => -79
	},
	{#State 112
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvargs' => 153,
			'openvarg' => 88
		}
	},
	{#State 113
		DEFAULT => -32
	},
	{#State 114
		DEFAULT => -30
	},
	{#State 115
		ACTIONS => {
			'SC' => 79
		},
		DEFAULT => -51,
		GOTOS => {
			'event_tail' => 154
		}
	},
	{#State 116
		DEFAULT => -81
	},
	{#State 117
		ACTIONS => {
			'LB' => 155
		}
	},
	{#State 118
		ACTIONS => {
			'LB' => 156
		}
	},
	{#State 119
		ACTIONS => {
			'LB' => 157
		}
	},
	{#State 120
		ACTIONS => {
			'CO' => 158,
			'WHEN' => 159
		},
		DEFAULT => -69,
		GOTOS => {
			'noninertial_tail' => 160
		}
	},
	{#State 121
		ACTIONS => {
			'LB' => 161
		},
		DEFAULT => -114
	},
	{#State 122
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 116,
			'fluentproplist' => 162
		}
	},
	{#State 123
		DEFAULT => -60
	},
	{#State 124
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 82,
			'evproplist' => 163
		}
	},
	{#State 125
		DEFAULT => -61
	},
	{#State 126
		ACTIONS => {
			'CO' => 158,
			'IF' => 165
		},
		DEFAULT => -71,
		GOTOS => {
			'initially_tail' => 164
		}
	},
	{#State 127
		DEFAULT => -39
	},
	{#State 128
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprops' => 166,
			'typeprop' => 97
		}
	},
	{#State 129
		DEFAULT => -40
	},
	{#State 130
		ACTIONS => {
			";" => 167
		}
	},
	{#State 131
		ACTIONS => {
			'LB' => 168
		}
	},
	{#State 132
		ACTIONS => {
			'CO' => 169
		},
		DEFAULT => -44
	},
	{#State 133
		ACTIONS => {
			'LB' => 170
		}
	},
	{#State 134
		DEFAULT => -48
	},
	{#State 135
		ACTIONS => {
			'RC' => 171
		}
	},
	{#State 136
		ACTIONS => {
			'VARNAME' => 103
		},
		GOTOS => {
			'type' => 172
		}
	},
	{#State 137
		DEFAULT => -18
	},
	{#State 138
		DEFAULT => -76
	},
	{#State 139
		DEFAULT => -109
	},
	{#State 140
		ACTIONS => {
			'LB' => 173
		}
	},
	{#State 141
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 139,
			'fluentexpr' => 174
		}
	},
	{#State 142
		DEFAULT => -103
	},
	{#State 143
		DEFAULT => -101
	},
	{#State 144
		DEFAULT => -100
	},
	{#State 145
		DEFAULT => -102
	},
	{#State 146
		DEFAULT => -93
	},
	{#State 147
		ACTIONS => {
			'LB' => 175,
			'CO' => -114,
			";" => -114
		},
		DEFAULT => -97
	},
	{#State 148
		ACTIONS => {
			'LB' => 176
		}
	},
	{#State 149
		ACTIONS => {
			'CO' => 177
		},
		DEFAULT => -74
	},
	{#State 150
		ACTIONS => {
			'NEop' => 178,
			'EQop' => 179
		}
	},
	{#State 151
		DEFAULT => -64
	},
	{#State 152
		DEFAULT => -96
	},
	{#State 153
		ACTIONS => {
			'RB' => 180,
			'CO' => 110
		}
	},
	{#State 154
		ACTIONS => {
			";" => 181
		}
	},
	{#State 155
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 182
		}
	},
	{#State 156
		ACTIONS => {
			'VARNAME' => 184,
			'VIOL' => 49,
			'ATOMNAME' => 185
		},
		GOTOS => {
			'evprop' => 183,
			'openvarg' => 186
		}
	},
	{#State 157
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 187
		}
	},
	{#State 158
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 188
		}
	},
	{#State 159
		ACTIONS => {
			'EQ' => 140,
			'NOT' => 141,
			'OBL' => 117,
			'POW' => 118,
			'PERM' => 119,
			'VARNAME' => 85,
			'ATOMNAME' => 147,
			'NE' => 148
		},
		GOTOS => {
			'fluentprop' => 139,
			'expr' => 146,
			'ne_op' => 145,
			'not_expr' => 144,
			'eq_op' => 143,
			'fluentexpr' => 142,
			'exprlist' => 189,
			'openvarg' => 150
		}
	},
	{#State 160
		ACTIONS => {
			";" => 190
		}
	},
	{#State 161
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvargs' => 191,
			'openvarg' => 88
		}
	},
	{#State 162
		ACTIONS => {
			'CO' => 158,
			'IF' => 107
		},
		DEFAULT => -73,
		GOTOS => {
			'rule_tail' => 192
		}
	},
	{#State 163
		ACTIONS => {
			'CO' => 106,
			'IF' => 107
		},
		DEFAULT => -73,
		GOTOS => {
			'rule_tail' => 193
		}
	},
	{#State 164
		ACTIONS => {
			";" => 194
		}
	},
	{#State 165
		ACTIONS => {
			'EQ' => 140,
			'NOT' => 197,
			'OBL' => 117,
			'POW' => 118,
			'PERM' => 119,
			'VARNAME' => 85,
			'ATOMNAME' => 147,
			'NE' => 148
		},
		GOTOS => {
			'fluentprop' => 139,
			'staticexpr' => 201,
			'ne_op' => 200,
			'staticexprlist' => 195,
			'eq_op' => 199,
			'sf_expr' => 198,
			'fluentexpr' => 196,
			'not_sf_expr' => 202,
			'openvarg' => 150
		}
	},
	{#State 166
		DEFAULT => -43
	},
	{#State 167
		DEFAULT => -41
	},
	{#State 168
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprop' => 203
		}
	},
	{#State 169
		ACTIONS => {
			'PERM' => 133,
			'ATOMNAME' => 62,
			'POW' => 131
		},
		GOTOS => {
			'etypeprops' => 204,
			'typeprop' => 134,
			'etypeprop' => 132
		}
	},
	{#State 170
		ACTIONS => {
			'ATOMNAME' => 62
		},
		GOTOS => {
			'typeprop' => 205
		}
	},
	{#State 171
		DEFAULT => -13
	},
	{#State 172
		DEFAULT => -20
	},
	{#State 173
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 206
		}
	},
	{#State 174
		DEFAULT => -104
	},
	{#State 175
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvargs' => 207,
			'openvarg' => 88
		}
	},
	{#State 176
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 208
		}
	},
	{#State 177
		ACTIONS => {
			'EQ' => 140,
			'NOT' => 141,
			'OBL' => 117,
			'POW' => 118,
			'PERM' => 119,
			'VARNAME' => 85,
			'ATOMNAME' => 147,
			'NE' => 148
		},
		GOTOS => {
			'fluentprop' => 139,
			'expr' => 209,
			'ne_op' => 145,
			'not_expr' => 144,
			'eq_op' => 143,
			'fluentexpr' => 142,
			'openvarg' => 150
		}
	},
	{#State 178
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 210
		}
	},
	{#State 179
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 211
		}
	},
	{#State 180
		DEFAULT => -98
	},
	{#State 181
		DEFAULT => -50
	},
	{#State 182
		ACTIONS => {
			'CO' => 212
		}
	},
	{#State 183
		ACTIONS => {
			'RB' => 213
		}
	},
	{#State 184
		ACTIONS => {
			'SC' => -99
		},
		DEFAULT => -80
	},
	{#State 185
		ACTIONS => {
			'SC' => -97,
			'LB' => 214
		},
		DEFAULT => -78
	},
	{#State 186
		ACTIONS => {
			'SC' => 215
		}
	},
	{#State 187
		ACTIONS => {
			'RB' => 216
		}
	},
	{#State 188
		DEFAULT => -82
	},
	{#State 189
		ACTIONS => {
			'CO' => 177
		},
		DEFAULT => -70
	},
	{#State 190
		DEFAULT => -68
	},
	{#State 191
		ACTIONS => {
			'RB' => 217,
			'CO' => 110
		}
	},
	{#State 192
		ACTIONS => {
			";" => 218
		}
	},
	{#State 193
		ACTIONS => {
			";" => 219
		}
	},
	{#State 194
		DEFAULT => -67
	},
	{#State 195
		ACTIONS => {
			'CO' => 220
		},
		DEFAULT => -72
	},
	{#State 196
		DEFAULT => -92
	},
	{#State 197
		ACTIONS => {
			'PERM' => 119,
			'ATOMNAME' => 121,
			'OBL' => 117,
			'POW' => 118
		},
		GOTOS => {
			'fluentprop' => 139,
			'sf_expr' => 221,
			'fluentexpr' => 196
		}
	},
	{#State 198
		DEFAULT => -90
	},
	{#State 199
		DEFAULT => -88
	},
	{#State 200
		DEFAULT => -89
	},
	{#State 201
		DEFAULT => -85
	},
	{#State 202
		DEFAULT => -87
	},
	{#State 203
		ACTIONS => {
			'RB' => 222
		}
	},
	{#State 204
		DEFAULT => -45
	},
	{#State 205
		ACTIONS => {
			'RB' => 223
		}
	},
	{#State 206
		ACTIONS => {
			'CO' => 224
		}
	},
	{#State 207
		ACTIONS => {
			'RB' => 225,
			'CO' => 110
		}
	},
	{#State 208
		ACTIONS => {
			'CO' => 226
		}
	},
	{#State 209
		DEFAULT => -94
	},
	{#State 210
		DEFAULT => -107
	},
	{#State 211
		DEFAULT => -105
	},
	{#State 212
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 227
		}
	},
	{#State 213
		DEFAULT => -111
	},
	{#State 214
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvargs' => 228,
			'openvarg' => 88
		}
	},
	{#State 215
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 229
		}
	},
	{#State 216
		DEFAULT => -110
	},
	{#State 217
		DEFAULT => -115
	},
	{#State 218
		DEFAULT => -66
	},
	{#State 219
		DEFAULT => -65
	},
	{#State 220
		ACTIONS => {
			'EQ' => 140,
			'NOT' => 197,
			'OBL' => 117,
			'POW' => 118,
			'PERM' => 119,
			'VARNAME' => 85,
			'ATOMNAME' => 147,
			'NE' => 148
		},
		GOTOS => {
			'fluentprop' => 139,
			'staticexpr' => 230,
			'ne_op' => 200,
			'eq_op' => 199,
			'sf_expr' => 198,
			'fluentexpr' => 196,
			'not_sf_expr' => 202,
			'openvarg' => 150
		}
	},
	{#State 221
		DEFAULT => -91
	},
	{#State 222
		DEFAULT => -47
	},
	{#State 223
		DEFAULT => -46
	},
	{#State 224
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 231
		}
	},
	{#State 225
		ACTIONS => {
			'CO' => -115,
			";" => -115
		},
		DEFAULT => -98
	},
	{#State 226
		ACTIONS => {
			'VARNAME' => 85,
			'ATOMNAME' => 87
		},
		GOTOS => {
			'openvarg' => 232
		}
	},
	{#State 227
		ACTIONS => {
			'CO' => 233
		}
	},
	{#State 228
		ACTIONS => {
			'RB' => 234,
			'CO' => 110
		}
	},
	{#State 229
		ACTIONS => {
			'RB' => 235
		}
	},
	{#State 230
		DEFAULT => -86
	},
	{#State 231
		ACTIONS => {
			'RB' => 236
		}
	},
	{#State 232
		ACTIONS => {
			'RB' => 237
		}
	},
	{#State 233
		ACTIONS => {
			'VARNAME' => 48,
			'VIOL' => 49,
			'ATOMNAME' => 50
		},
		GOTOS => {
			'evprop' => 238
		}
	},
	{#State 234
		ACTIONS => {
			'SC' => -98
		},
		DEFAULT => -79
	},
	{#State 235
		DEFAULT => -112
	},
	{#State 236
		DEFAULT => -106
	},
	{#State 237
		DEFAULT => -108
	},
	{#State 238
		ACTIONS => {
			'RB' => 239
		}
	},
	{#State 239
		DEFAULT => -113
	}
],
                                  yyrules  =>
[
	[#Rule 0
		 '$start', 2, undef
	],
	[#Rule 1
		 'top', 1, undef
	],
	[#Rule 2
		 'top', 1, undef
	],
	[#Rule 3
		 'instal', 5, undef
	],
	[#Rule 4
		 'instal', 1, undef
	],
	[#Rule 5
		 'instal', 1, undef
	],
	[#Rule 6
		 'multi', 3, undef
	],
	[#Rule 7
		 'query', 4, undef
	],
	[#Rule 8
		 'qexpr', 1, undef
	],
	[#Rule 9
		 'qexpr', 1, undef
	],
	[#Rule 10
		 'qexpr', 1, undef
	],
	[#Rule 11
		 'exists_expr', 4, undef
	],
	[#Rule 12
		 'never_expr', 4, undef
	],
	[#Rule 13
		 'forall_expr', 7, undef
	],
	[#Rule 14
		 'grounding', 2, undef
	],
	[#Rule 15
		 'mname', 3,
sub
#line 57 "instal.yp"
{
		$_[0]->inst->set_name('multi');

	  }
	],
	[#Rule 16
		 'name', 3,
sub
#line 62 "instal.yp"
{
		$_[0]->inst->set_name( $_[2] );

		  my $itype = $_[0]->lp->typeInst();
		  $_[0]->lp->add_literal( $_[0]->inst->varname, $itype );

	  }
	],
	[#Rule 17
		 'typeprop', 1,
sub
#line 73 "instal.yp"
{ InstAL::Atom->new( $_[1] ) }
	],
	[#Rule 18
		 'typeprop', 4,
sub
#line 74 "instal.yp"
{ InstAL::Atom->new( $_[1], $_[3] ) }
	],
	[#Rule 19
		 'typeargs', 1,
sub
#line 76 "instal.yp"
{ [ $_[1] ] }
	],
	[#Rule 20
		 'typeargs', 3,
sub
#line 77 "instal.yp"
{ push @{ $_[1] }, $_[3]; $_[1] }
	],
	[#Rule 21
		 'type', 1,
sub
#line 79 "instal.yp"
{ $_[0]->resolveType( $_[1] ) }
	],
	[#Rule 22
		 'iflags', 0, undef
	],
	[#Rule 23
		 'iflags', 2, undef
	],
	[#Rule 24
		 'iflag', 1, undef
	],
	[#Rule 25
		 'default_rule', 3,
sub
#line 89 "instal.yp"
{
		$_->[0]->inst->add_flags( [ $_[1], $_[2] ] );
	  }
	],
	[#Rule 26
		 'defl_arg', 1, undef
	],
	[#Rule 27
		 'defl_arg', 1, undef
	],
	[#Rule 28
		 'typedefs', 0, undef
	],
	[#Rule 29
		 'typedefs', 2, undef
	],
	[#Rule 30
		 'typedef', 4,
sub
#line 98 "instal.yp"
{
		$_[0]->addType( $_[2], $_[3] );

	  }
	],
	[#Rule 31
		 'super_typedef', 0,
sub
#line 104 "instal.yp"
{ $_[0]->lp->typeANY }
	],
	[#Rule 32
		 'super_typedef', 2,
sub
#line 105 "instal.yp"
{
		my $type = $_[0]->lp->get_types( $_[2], 1 );

		if ( !$type ) {
			$_[0]->lp->error( "Unable to resolve undeclared type $_[2]",
				$_[0]->curLoc );
		}

# 	if(!$type->isSubType($_[0]->lp->typeDomFluent)){
# 	    $_[0]->lp->warn("Invalid super class".$type->typeName.": cannot extend internal types",$_[0]->curLoc);
# 	}
		$type;
	}
	],
	[#Rule 33
		 'decls', 0, undef
	],
	[#Rule 34
		 'decls', 2, undef
	],
	[#Rule 35
		 'decl', 1, undef
	],
	[#Rule 36
		 'decl', 1, undef
	],
	[#Rule 37
		 'mevents', 0, undef
	],
	[#Rule 38
		 'mevents', 2, undef
	],
	[#Rule 39
		 'fluentdecl', 3,
sub
#line 134 "instal.yp"
{
		foreach my $fl ( @{ $_[2] } )
		  { $_[0]->add_fluent( $fl->name, [], $fl->args ) }
	  }
	],
	[#Rule 40
		 'fluentdecl', 3,
sub
#line 137 "instal.yp"
{
		foreach my $fl ( @{ $_[2] } )
		  { $_[0]->add_fluent( $fl->name, ['static'], $fl->args ) }
	  }
	],
	[#Rule 41
		 'fluentdecl', 4,
sub
#line 140 "instal.yp"
{
		foreach my $fl ( @{ $_[3] } )
		  { $_[0]->add_fluent( $fl->name, ['noninertial'], $fl->args ) }
	  }
	],
	[#Rule 42
		 'typeprops', 1,
sub
#line 146 "instal.yp"
{ [ $_[1] ]; }
	],
	[#Rule 43
		 'typeprops', 3,
sub
#line 147 "instal.yp"
{ push @{ $_[2] }, $_[1]; }
	],
	[#Rule 44
		 'etypeprops', 1,
sub
#line 150 "instal.yp"
{ [ $_[1] ]; }
	],
	[#Rule 45
		 'etypeprops', 3,
sub
#line 151 "instal.yp"
{ push @{ $_[2] }, $_[1]; }
	],
	[#Rule 46
		 'etypeprop', 4,
sub
#line 154 "instal.yp"
{ InstAL::Atom->new( $_[1], [ $_[3] ] ) }
	],
	[#Rule 47
		 'etypeprop', 4,
sub
#line 155 "instal.yp"
{ InstAL::Atom->new( $_[1], [ $_[3] ] ) }
	],
	[#Rule 48
		 'etypeprop', 1, undef
	],
	[#Rule 49
		 'meventdecl', 5,
sub
#line 157 "instal.yp"
{
		$_[0]
		  ->add_event( $_[3]->name, $_[1], $_[3]->args, $_[4], { multi => 1 } );
	  }
	],
	[#Rule 50
		 'eventdecl', 5,
sub
#line 162 "instal.yp"
{
		my $flags  = {};
		  my $type = $_[1];
		  if ( $type eq 'CREATE' ) {
			$type = InstAL::Event::EXO();
			$flags->{create} = 1;
		}
		elsif ( $type eq 'DEST' ) {
			$type = InstAL::Event::INST();
			$flags->{dest} = 1;
		}
		$_[0]->add_event( $_[3]->name, $type, $_[3]->args, $_[4], $flags )
	  }
	],
	[#Rule 51
		 'event_tail', 0,
sub
#line 176 "instal.yp"
{ $_[0]->lp->typeEvent }
	],
	[#Rule 52
		 'event_tail', 2,
sub
#line 177 "instal.yp"
{
		my $type = $_[0]->lp->get_types( $_[2], 1 );

		if ( !$type ) {
			$_[0]->lp->error( "Unable to resolve undeclared type $_[2]",
				$_[0]->curLoc );
		}
		if ( !$type->isSubType( $_[0]->lp->typeEvent ) ) {
			$_[0]->lp->error(
				"Cannot define event as a subclass of a non-event class",
				$_[0]->curLoc );
		}
		$type;
	}
	],
	[#Rule 53
		 'evtype', 1,
sub
#line 192 "instal.yp"
{ InstAL::Event::EXO() }
	],
	[#Rule 54
		 'evtype', 1,
sub
#line 193 "instal.yp"
{ InstAL::Event::INST() }
	],
	[#Rule 55
		 'evtype', 1,
sub
#line 194 "instal.yp"
{ InstAL::Event::VIOL() }
	],
	[#Rule 56
		 'evtype', 1,
sub
#line 195 "instal.yp"
{ 'CREATE' }
	],
	[#Rule 57
		 'evtype', 1,
sub
#line 195 "instal.yp"
{ 'DEST' }
	],
	[#Rule 58
		 'rules', 0, undef
	],
	[#Rule 59
		 'rules', 2, undef
	],
	[#Rule 60
		 'causal_type', 1,
sub
#line 200 "instal.yp"
{ InstAL::CausalRule::INITIATES }
	],
	[#Rule 61
		 'causal_type', 1,
sub
#line 201 "instal.yp"
{ InstAL::CausalRule::TERMINATES }
	],
	[#Rule 62
		 'mrules', 0, undef
	],
	[#Rule 63
		 'mrules', 2, undef
	],
	[#Rule 64
		 'mrule', 5,
sub
#line 206 "instal.yp"
{

		my $rule = InstAL::MultiGeneratesRule->new( $_[1], $_[3], $_[4] );
		  $_[0]->recordFrag($rule);
		  $_[0]->inst->add_genrules($rule);
	  }
	],
	[#Rule 65
		 'rule', 5,
sub
#line 214 "instal.yp"
{
		my $rule = InstAL::GeneratesRule->new( $_[1], $_[3], $_[4] );
		  $_[0]->recordFrag($rule);
		  $_[0]->inst->add_genrules($rule);
	  }
	],
	[#Rule 66
		 'rule', 5,
sub
#line 218 "instal.yp"
{
	      foreach my $fref (@{$_[3]}){
	  		if(defined($fref->fluent()) && $fref->fluent()->noninertial()==1){
	    		 $_[0]->lp->error("Cannot use ".$_[2]." on a noninertial fluent ".$fref->name,$_[0]->curLoc());	
	  		}
 	  	  }
	  	
		my $rule = InstAL::CausalRule->new( $_[2], $_[1], $_[3], $_[4] );
		  $_[0]->recordFrag($rule);
		  if ( $_[2] ) {
			$_[0]->inst->add_termrules($rule);
		}
		else {
			$_[0]->inst->add_initrules($rule);
		}
	  }
	],
	[#Rule 67
		 'rule', 4,
sub
#line 233 "instal.yp"
{
	  	  foreach my $fref (@{$_[2]}){
	  	  	
	  		if(defined($fref->fluent()) && $fref->fluent()->noninertial()==1){
	    		 $_[0]->lp->error("Cannot use initially on a noninertial fluent ".$fref->name,$_[0]->curLoc());	
	  		}
 	  	  }
	
		my $rule = InstAL::InitialRule->new( $_[2], $_[3] );
		  $_[0]->recordFrag($rule);
		  $_[0]->inst->add_initiallys($rule)
	  }
	],
	[#Rule 68
		 'rule', 4,
sub
#line 244 "instal.yp"
{
	  	foreach my $fref (@{$_[2]}){
	  		if(defined($fref->fluent()) && $fref->fluent()->noninertial()==0){
	    		 $_[0]->lp->error("Cannot use always on an inertial fluent ".$fref->name,$_[0]->curLoc());	
	  		}
	  	}
		my $rule = InstAL::DynamicRule->new( $_[2], $_[3] );
		  $_[0]->recordFrag($rule);
		  $_[0]->inst->add_dynamics($rule);
	  }
	],
	[#Rule 69
		 'noninertial_tail', 0,
sub
#line 255 "instal.yp"
{ new InstAL::Expr::TrueExpr() }
	],
	[#Rule 70
		 'noninertial_tail', 2,
sub
#line 256 "instal.yp"
{ $_[0]->recordFrag( $_[2] ) }
	],
	[#Rule 71
		 'initially_tail', 0,
sub
#line 258 "instal.yp"
{ new InstAL::Expr::TrueExpr() }
	],
	[#Rule 72
		 'initially_tail', 2,
sub
#line 259 "instal.yp"
{ $_[0]->recordFrag( $_[2] ) }
	],
	[#Rule 73
		 'rule_tail', 0,
sub
#line 261 "instal.yp"
{ new InstAL::Expr::TrueExpr() }
	],
	[#Rule 74
		 'rule_tail', 2,
sub
#line 262 "instal.yp"
{ $_[0]->recordFrag( $_[2] ) }
	],
	[#Rule 75
		 'evproplist', 1,
sub
#line 265 "instal.yp"
{ [ $_[1] ] }
	],
	[#Rule 76
		 'evproplist', 3,
sub
#line 267 "instal.yp"
{ push @{ $_[1] }, $_[0]->recordFrag( $_[3] ); $_[1]; }
	],
	[#Rule 77
		 'evprop', 4,
sub
#line 270 "instal.yp"
{ $_[0]->newevref( '_viol_' . $_[3]->eventname, $_[3]->args, 1 ) }
	],
	[#Rule 78
		 'evprop', 1,
sub
#line 271 "instal.yp"
{ $_[0]->newevref( $_[1], [] ) }
	],
	[#Rule 79
		 'evprop', 4,
sub
#line 272 "instal.yp"
{ $_[0]->newevref( $_[1], $_[3] ) }
	],
	[#Rule 80
		 'evprop', 1,
sub
#line 272 "instal.yp"
{
		$_[0]->recordFrag(
			new InstAL::Ref::EventVarRef( $_[1], $_[0]->lp->get_types('Event') )
		);
	}
	],
	[#Rule 81
		 'fluentproplist', 1,
sub
#line 278 "instal.yp"
{ [ $_[1] ] }
	],
	[#Rule 82
		 'fluentproplist', 3,
sub
#line 280 "instal.yp"
{ push @{ $_[1] }, $_[0]->recordFrag( $_[3] ); $_[1]; }
	],
	[#Rule 83
		 'varg', 1, undef
	],
	[#Rule 84
		 'varg', 1,
sub
#line 287 "instal.yp"
{ $_[0]->morphVar( $_[1] ) }
	],
	[#Rule 85
		 'staticexprlist', 1, undef
	],
	[#Rule 86
		 'staticexprlist', 3,
sub
#line 289 "instal.yp"
{
		if ( ref( $_[1] ) eq 'InstAL::Expr::AndExpr' ) {
			$_[1]->add_subs( $_[3] );
			return $_[1];
		}
		else {
			return $_[0]
			  ->recordFrag( InstAL::Expr::AndExpr->new( [ $_[1], $_[3] ] ) );
		}
	  }
	],
	[#Rule 87
		 'staticexpr', 1, undef
	],
	[#Rule 88
		 'staticexpr', 1, undef
	],
	[#Rule 89
		 'staticexpr', 1, undef
	],
	[#Rule 90
		 'staticexpr', 1, undef
	],
	[#Rule 91
		 'not_sf_expr', 2,
sub
#line 306 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::NotExpr( $_[2] ) ) }
	],
	[#Rule 92
		 'sf_expr', 1,
sub
#line 308 "instal.yp"
{
		$_[1]->fluentref->set_static(1);
		$_[1];
	  }
	],
	[#Rule 93
		 'exprlist', 1, undef
	],
	[#Rule 94
		 'exprlist', 3,
sub
#line 316 "instal.yp"
{
		if ( ref( $_[1] ) eq 'InstAL::Expr::AndExpr' ) {
			$_[1]->add_subs( $_[3] );
			return $_[1];
		}
		else {
			return $_[0]
			  ->recordFrag( InstAL::Expr::AndExpr->new( [ $_[1], $_[3] ] ) );
		}
	  }
	],
	[#Rule 95
		 'openvargs', 1,
sub
#line 327 "instal.yp"
{ [ $_[1] ] }
	],
	[#Rule 96
		 'openvargs', 3,
sub
#line 328 "instal.yp"
{ push @{ $_[1] }, $_[3]; $_[1] }
	],
	[#Rule 97
		 'openvarg', 1,
sub
#line 332 "instal.yp"
{
		$_[0]->recordFrag(
			new InstAL::Ref::LiteralRef( $_[1], $_[0]->lp->typeANY() ) );
	}
	],
	[#Rule 98
		 'openvarg', 4,
sub
#line 336 "instal.yp"
{
		$_[0]->recordFrag(
			new InstAL::Ref::VarLiteralRef( $_[1], $_[3], $_[0]->lp->typeANY() )
		);
	}
	],
	[#Rule 99
		 'openvarg', 1,
sub
#line 341 "instal.yp"
{
		$_[0]->recordFrag(
			new InstAL::Ref::VarRef(
				$_[0]->morphVar( $_[1] ),
				$_[0]->lp->typeANY()
			)
		);
	}
	],
	[#Rule 100
		 'expr', 1, undef
	],
	[#Rule 101
		 'expr', 1, undef
	],
	[#Rule 102
		 'expr', 1, undef
	],
	[#Rule 103
		 'expr', 1, undef
	],
	[#Rule 104
		 'not_expr', 2,
sub
#line 353 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::NotExpr( $_[2] ) ) }
	],
	[#Rule 105
		 'eq_op', 3,
sub
#line 356 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::EqExpr( $_[1], $_[3] ) ) }
	],
	[#Rule 106
		 'eq_op', 6,
sub
#line 358 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::EqExpr( $_[3], $_[5] ) ) }
	],
	[#Rule 107
		 'ne_op', 3,
sub
#line 361 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::NeExpr( $_[1], $_[3] ) ) }
	],
	[#Rule 108
		 'ne_op', 6,
sub
#line 363 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Expr::NeExpr( $_[3], $_[5] ) ) }
	],
	[#Rule 109
		 'fluentexpr', 1,
sub
#line 367 "instal.yp"
{
		$_[0]->recordFrag( new InstAL::Expr::FluentExpr( $_[1] ) );
	  }
	],
	[#Rule 110
		 'fluentprop', 4,
sub
#line 373 "instal.yp"
{ $_[0]->recordFrag( new InstAL::Ref::PermRef( $_[3] ) ) }
	],
	[#Rule 111
		 'fluentprop', 4,
sub
#line 374 "instal.yp"
{
		$_[0]->recordFrag(
			new InstAL::Ref::PowRef(
				new InstAL::Ref::LiteralRef( $_[0]->inst->varname, 1 ), $_[3]
			)
		);
	}
	],
	[#Rule 112
		 'fluentprop', 6,
sub
#line 381 "instal.yp"
{
		$_[3]->set_type( $_[0]->lp->get_types('Inst') );
		$_[0]->recordFrag( new InstAL::Ref::PowRef( $_[3], $_[5] ) );
	}
	],
	[#Rule 113
		 'fluentprop', 8,
sub
#line 385 "instal.yp"
{
		$_[0]->recordFrag( new InstAL::Ref::OblRef( $_[3], $_[5], $_[7] ) );
	}
	],
	[#Rule 114
		 'fluentprop', 1,
sub
#line 388 "instal.yp"
{ $_[0]->newfluentref( $_[1], [] ) }
	],
	[#Rule 115
		 'fluentprop', 4,
sub
#line 389 "instal.yp"
{ $_[0]->newfluentref( $_[1], $_[3] ) }
	]
],
                                  @_);
    bless($self,$class);
}

#line 391 "instal.yp"


	  my @TOKENS = (
		[ '(multi)',       'MULTI',       'multi' ],
		[ '(institution)', 'INSTITUTION', 'institution' ],
		[ '(default\s)',   'DEFAULT',     'default' ],
		[ '(deny)',        'DENY',        'deny' ],
		[ '(fluent)',      'FLUENT',      'fluent' ],
		[ '(noninertial)', 'NONINERTIAL', 'noninertial' ],
		[ '(type)',        'TYPE',        'fluent' ],
		[ '(action)',      'INST',        'action' ],
		[ '(event)',       'EVENT',       'event' ],
		[ '(create\s)',    'CREATE',      'create' ],
		[ '(dest)\s',      'DEST',        'dest' ],
		[ '(unique)',      'UNIQUE',      'unique' ],
		[ '(static)',      'STATIC',      'static' ],
		[ '(exogenous)',   'EXOGENOUS',   'exogenous' ],

		#    ['(ex)[^\w]','EXO','ex'],
		[ '(violation)',  'VIOLATION',  'violation' ],
		[ '(generates)',  'GENERATES',  'generates' ],
		[ '(initiates)',  'INITIATES',  'initiates' ],
		[ '(terminates)', 'TERMINATES', 'terminates' ],
		[ '(when)',       'WHEN',       'when' ],
		[ '(always)',     'ALWAYS',     'always' ],
		[ '(initially)',  'INITIALLY',  'initially' ],
		[ '(exogenous)',  'EXOGENOUS',  'exogenous' ],
		[ '(inst)',       'INST',       'inst' ],
		[ '(viol)',       'VIOL',       'viol' ],
		[ '(forbid)',     'FORBID',     'forbid' ],
		[ '(permit)',     'PERMIT',     'permit' ],
		[ '(perm)',       'PERM',       'perm' ],
		[ '(pow)',        'POW',        'pow' ],
		[ '(obl)',        'OBL',        'obl' ],
		[ '(not)',        'NOT',        'not' ],
		[ '(\!=)',        'NEop',       '!=' ],
		[ '(=)',          'EQop',       '=' ],
		[ '(if)',         'IF',         'if' ],
		[ '(\()',         'LB',         '(' ],
		[ '(\))',         'RB',         ')' ],
		[ '(,)',          'CO',         ',' ],
		[ '(\:)',         'SC',         ':' ],
		[ '(;)',          ';',          ';' ],
		[
			'(([A-Z][A-Za-z0-9_]*)|(\_))[^A-Za-z0-9_]', 'VARNAME',
			'[V]ariable name'
		],
		[
			'([a-z0-9][A-Za-z0-9_]*)[^A-Za-z0-9_]', 'ATOMNAME', '[l]iteral name'
		],
		[ '(query)',  'QUERY',  'query' ],
		[ '(domain)', 'DOMAIN', 'domain' ]
	  );

	  sub col {
		my $self = shift;
		return $self->{col};
	}

	sub parseStream {
		my $self     = shift;
		my $stream   = shift;
		my $lp       = shift or die "No LP";
		my $filename = shift;

		$self->{-lp}    = $lp;
		$self->{-input} = $stream;
		print "Creating Institution\n" if $DEBUG;
		$self->{-inst}     = InstAL::Institution->new($lp);
		$self->{-filename} = $filename;
		$self->{lineno}    = 0;
		$self->lp->set_lines( $filename, [] );

		print "About to run parser " if $DEBUG;

		#    eval {
		$self->YYParse( yylex => \&_Lexer, yyerror => \&_Error );

		#   }; if($@){
		#	print "inst: ", Dumper($self->inst) if $DEBUG;
		#	print "died $@ \n";
		#    }

		return $self->inst;

	}

	sub _Lexer {
		my $self = shift;

		my $fh = $self->{-input};
		while (1) {

			#	print STDERR "Lexer called ($fh)\n";
			$self->YYData->{INPUT}
			  or $self->YYData->{INPUT} = $self->readline
			  or return ( '', undef );

			$self->YYData->{INPUT} =~ s/%.*//;
			chomp $self->YYData->{INPUT};
			print STDERR "Lexing \"", $self->YYData->{INPUT}, "\"\n" if $DEBUG;
			my $prev = $self->YYData->{INPUT};
			foreach my $l (@TOKENS) {
				my $h = $l->[0];

				$self->YYData->{INPUT} =~ s/^(\s+)//;
				$self->{col} += length($1);

				if ( $self->YYData->{INPUT} =~ /^$h/ ) {
					$self->YYData->{INPUT} =
					  substr( $self->YYData->{INPUT}, length($1) );
					$self->{col} += length($1);
					print STDERR "Got token ", $l->[1], "($1)\n" if $DEBUG;
					return ( $l->[1], $1 );
				}
			}

			if ( $prev && $prev eq $self->YYData->{INPUT} ) {
				$self->lp->error( "Unexpected token or missing input at $prev",
					$self->curLoc );
			}

		}
	}

	sub getTokDesc {
		my $self = shift;
		my $name = shift;
		return "eof" if !$name;
		foreach my $tok (@TOKENS) {

			#	print STDERR "checking ",$tok->[1],"\n";
			if ( $name eq $tok->[1] ) {
				return $tok->[2];
			}
		}
		die "Token '$name' not found";
	}

	sub _Error {

		#    print STDERR "args are ",join(",",@_),"\n";
		my $self = shift;

		my ($value) = $self->YYCurval;
		my ($what)  = $self->YYCurtok;
		my @expects = map { $self->getTokDesc($_) } ( $self->YYExpect );

		my $cf =
		  InstAL::CodeFragment->new( $self->filename, $self->line, $self->col );

		$self->lp->error(
			"Syntax error: unexpected token \'$value\' was expecting one of ("
			  . join( ",", map { "'" . $_ . "'" } (@expects) ) . ")\n",
			$cf
		);

	}

	sub inst {
		my $self = shift;
		return $self->{-inst};
	}

	sub newevref {
		my $self   = shift;
		my $evname = shift;
		my $args   = shift;
		my $isviol = shift;

		my $evref;
		if ($isviol) {
			$evref = InstAL::Ref::ViolEventRef->new( $evname, $args );
		}
		else {
			$evref = InstAL::Ref::EventRef->new( $evname, $args );
		}
		$self->recordFrag($evref);
		$self->lp->add_evrefs($evref);
		return $evref;
	}

	sub filename {
		my $self = shift;
		return $self->{-filename};
	}

	sub addType {
		my $self      = shift;
		my $typename  = shift;
		my $supertype = shift;

		my $type;
		$type = $self->lp->get_types( $typename, 1 );
		if ($type) {
			if ( $type->super != $supertype ) {
				$self->lp->error(
"Type declaration of $typename has incompatible super type (got "
					  . $supertype->typeName
					  . " was expecting "
					  . $type->super->typeName
					  . " previous declaration:"
					  . $supertype->getLocStr,
					$self->curLoc
				);

			}

		}
		else {
			print STDERR "Adding new type $typename\n" if $DEBUG;
			my $type = new InstAL::Type( $typename, 0, $supertype );
			$self->recordFrag($type);
			$self->lp->addType($type);
			$self->inst->add_type($type);
		}
	}

	sub newfluentref {
		my $self       = shift;
		my $fluentname = shift;
		my $args       = shift;

		my $f = InstAL::Ref::FluentRef->new( $fluentname, $args );
		$self->recordFrag($f);
		$self->lp->add_fluentrefs($f);
		return $f;
	}

	sub lp {
		my $self = shift;
		return $self->{-lp};
	}

	sub readline {
		my $self = shift;

		#    $self->{lineno} = 0 if(!exists($self->{lineno}));
		$self->{lineno}++;
		$self->{col} = 0;
		my $fh   = $self->{-input};
		my $line = $fh->getline;

		#    chomp $line;
		my $cl = $line;
		chomp $cl;
		push @{ $self->lp->lines->{ $self->filename } }, $line;

		return $line;
	}

	sub newliteralref {
		my $self       = shift;
		my $fluentname = shift;
		my $args       = shift;
		return undef;
	}

	sub curLoc {
		my $self = shift;
		my $loc =
		  InstAL::CodeFragment->new( $self->filename, $self->line, $self->col );
		return $loc;
	}

	sub recordFrag {
		my $self = shift;
		my $frag = shift;
		$frag->setLoc( $self->filename, $self->line, $self->col );
		return $frag;
	}

	sub line {
		my $self = shift;
		return $self->{lineno};
	}

	sub add_event {
		my $self  = shift;
		my $name  = shift;
		my $type  = shift;
		my $args  = shift;
		my $super = shift;
		my $flags = shift || {};

		print STDERR "Adding event $name,$type,$super,$flags\n" if $DEBUG ;
		my $inst  = $self->inst;
		my $other = $self->lp->get_events($name);

		if ( $flags->{multi} && $other ) {
			print "*** Ading event $name to multi\n" if $DEBUG;
			$other->flags->{multi} = 1;
			$inst->set_events( $name, $other );
			return $other;
		}

		$self->lp->error(
			"Event \"$name\" already defined at " . $other->getLocStr(),
			$self->curLoc )
		  if $other;

		my $ev =
		  InstAL::Event->new( $name, $type, $args, $inst, $super, $flags );

		$self->lp->addType($ev);
		$inst->set_events( $name, $ev );
		$self->recordFrag($ev);
		$self->lp->set_events( $name, $ev );

		if ( $type ne 'violation' ) {
			my $vi = InstAL::ViolEvent->new( $ev, $inst );
			$vi->copyLocFrom($ev);
			$self->lp->addType($vi);
			$inst->set_events( '_viol_' . $name, $vi );
			$self->lp->set_events( '_viol_' . $name, $vi );

		}

        print STDERR "Adding extra fluents to $name with type '$type'\n" if $DEBUG;
		if ( $type eq 'inst' || $type eq 'dest' || $type eq 'exogenous') {
			my $permname = '_PERM_' . $name;
			print STDERR "Adding perm  fluent $permname\n";
			my $permf = InstAL::Fluent->new( $permname, $args, [], $inst );
			$self->lp->addType($permf);
			$self->recordFrag($permf);
			$inst->set_fluents( $permname, $permf );
			$self->lp->set_fluents( $permname, $permf );
		}
		
		if($type eq 'inst' || $type eq 'dest' ){
			my $powname = '_POW_' . $name;
			print STDERR "Adding pow fluent $powname\n";
			my $powf = InstAL::Fluent->new( $powname, $args, [], $inst );
			$self->lp->addType($powf);
			$self->recordFrag($powf);
			$inst->set_fluents( $powname, $powf );
			$self->lp->set_fluents( $powname, $powf );
			
		}

	}

	# name : string
	# args : [Type, Type...]
	# flags :[String, String]
	sub add_fluent {
		my $self  = shift;
		my $name  = shift;
		my $flags = shift;
		my $args  = shift;
		my $inst  = $self->inst;

		my $other = $self->lp->get_fluents( $name, $args );

		if ( $name eq 'perm' || $name eq 'pow'  ) {
			if(grep 'noninertial',@{$flags}){
			  print "Got flagging of norm fluent as noninertial", Dumper($args) if $DEBUG;
			  
			  my $eventname = $args->[0]->{-name};
			  my $event = $self->lp->get_events($eventname);
			  if(!defined($event)){
			  	$self->lp->error( "Trying to change inertiality of event $event but this event cannot be found",
					$self->curLoc );
			  }
			  my $fluent ;
			  if($name eq 'perm'){
			  	$fluent = $self->lp->get_fluents($event->permFluentName());
			  }elsif($name  eq'pow'){			  	
			  	$fluent = $self->lp->get_fluents($event->powFluentName());
			  }
			  
			  $fluent->set_noninertial(1);
	         }else{
			  	$self->lp->error("Cannot (re-)define inertial fluent $name - it is defined implicitly!\n",$self->curLoc);
			 }
		 
		}else{
			
			if($other){
			$self->lp->error(
				"Fluent \"$name\" already defined at " . $other->getLocStr(),
				$self->curLoc );
			}
	
			print STDERR "Added Fluent $name\n" if $DEBUG;
			my $f = InstAL::Fluent->new( $name, $flags, $args, $inst );
			$self->lp->addType($f);
			$self->recordFrag($f);
			$inst->set_fluents( $name, $f );
			$self->lp->set_fluents( $name, $f );
		}
	}

	sub newvarref {
		my $self       = shift;
		my $fluentname = shift;
		my $args       = shift;
		return undef;
	}

	sub resolveType {
		my $self     = shift;
		my $typename = shift;

		if ( my $type = $self->lp->get_types($typename) ) {
			return $type;
		}
		die "Unable to resove type $typename";
	}

	my $intvcount = 0;

=head1 morphVar
  hack to deal with "_" variables, replaces "_" with an internal variable;
  for other variables just returns the original name;
=cut

	  sub morphVar {
		my $self    = shift;
		my $varname = shift;
		return $varname if $varname ne '_';
		return sprintf( "INTVAR%.3d", $intvcount++ );
	}

1;
