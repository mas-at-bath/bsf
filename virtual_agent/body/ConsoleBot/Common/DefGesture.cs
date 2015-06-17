/*
 * DefGesture.cs
 * 
 *      20-12-2011
 *      Created by JeeHang (jeehanglee@gmail.com)
 *      
 *      Defines action categories
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using OpenMetaverse;

namespace ConsoleBot
{
    public static class DefGesture
    {
        /// <summary>Agent with afraid expression on face</summary>
        public readonly static UUID AFRAID = new UUID("ff201a24-6893-4c41-77f2-bf707481e585");

        /// <summary>Agent with angry expression on face</summary>
        public readonly static UUID ANGRY = new UUID("5747a48e-073e-c331-f6f3-7c2149613d3e");
        
        /// <summary>Agent hunched over (away)</summary>
        public readonly static UUID AWAY = new UUID("66ecd9bd-22e1-2633-f41e-ad1a38205662");
        
        /// <summary>Agent doing a backflip</summary>
        public readonly static UUID BACKFLIP = new UUID("c4ca6188-9127-4f31-0158-23c4e2f93304");
        
        /// <summary>Agent laughing while holding belly</summary>
        public readonly static UUID BELLY_LAUGH = new UUID("ad22cda1-00c1-21cc-2f35-9ea5b8636f9e");
        
        /// <summary>Agent blowing a kiss</summary>
        public readonly static UUID BLOW_KISS = new UUID("db84829b-462c-ee83-1e27-9bbee66bd624");
        
        /// <summary>Agent with bored expression on face</summary>
        public readonly static UUID BORED = new UUID("b906c4ba-703b-1940-32a3-0c7f7d791510");
        
        /// <summary>Agent bowing to audience</summary>
        public readonly static UUID BOW = new UUID("44a87e64-2d78-14b7-1cde-ac0f5031621f");
        
        /// <summary>Agent brushing himself/herself off</summary>
        public readonly static UUID BRUSH = new UUID("349a3801-54f9-bf2c-3bd0-1ac89772af01");
        
        /// <summary>Agent in busy mode</summary>
        public readonly static UUID BUSY = new UUID("efcf670c-2d18-8128-973a-034ebc806b67");
        
        /// <summary>Agent clapping hands</summary>
        public readonly static UUID CLAP = new UUID("04608b19-1668-399c-5790-b7df8d9dc9cf");

        /// <summary>Agent crying</summary>
        public readonly static UUID CRY = new UUID("032ac149-dacd-e88a-5397-b9996b58f278");
        
        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE1 = new UUID("dbd054f1-132b-cc22-e46a-bad83c230c91");
        
        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE2 = new UUID("b57c996d-cb06-43c8-749d-e9ce361f172e");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE3 = new UUID("2d03fd78-92fc-ca52-aabb-a4904e1339d1");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE4 = new UUID("00b48fa0-ac43-fadf-d061-6b726a24a90a");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE5 = new UUID("76d30ee1-c369-12ec-8dbc-9cf2f51120ff");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE6 = new UUID("40b92916-8055-2562-008f-58924bd0e03c");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE7 = new UUID("0a3336cf-3ad1-012a-91c2-5b5620aaaef3");

        /// <summary>Agent dancing</summary>
        public readonly static UUID DANCE8 = new UUID("6212ffdd-d447-a92f-18f3-8cb94a799d83");

        /// <summary>Agent with afraid expression on face</summary>
        public readonly static UUID EXPRESS_AFRAID = new UUID("ff201a24-6893-4c41-77f2-bf707481e585");
        
        /// <summary>Agent with angry expression on face - Boo</summary>
        public readonly static UUID EXPRESS_ANGER = new UUID("90bdcd26-2c6b-44a7-aab2-18c5a04f3fc3");
        
        /// <summary>Agent with bored expression on face</summary>
        public readonly static UUID EXPRESS_BORED = new UUID("9583adac-bc9b-a021-ab8f-26018db46ab5");

        /// <summary>Agent crying</summary>
        public readonly static UUID EXPRESS_CRY = new UUID("a166cdc3-ea15-0c01-ca5b-d3fed6e3ca75");

        /// <summary>Agent showing disdain (dislike) for something - female</summary>
        public readonly static UUID EXPRESS_DISDAIN = new UUID("25252e5b-394b-fdab-76ee-28980cd62f81");
        
        /// <summary>Agent with embarassed expression on face</summary>
        public readonly static UUID EXPRESS_EMBARRASSED = new UUID("4f23cc4a-712e-aabc-afcf-3cab4c1164ea");
        
        /// <summary>Agent with frowning expression on face - female</summary>
        public readonly static UUID EXPRESS_FROWN = new UUID("4c450f9e-07c0-2f5a-bc89-57de56db8ba1");

        /// <summary>Agent with kissy face</summary>
        public readonly static UUID EXPRESS_KISS = new UUID("1e8d90cc-a84e-e135-884c-7c82c8b03a14");

        /// <summary>Agent expressing laughgter</summary>
        public readonly static UUID EXPRESS_LAUGH = new UUID("62570842-0950-96f8-341c-809e65110823");
        
        /// <summary>Agent with open mouth</summary>
        public readonly static UUID EXPRESS_OPEN_MOUTH = new UUID("5480a924-8b06-9551-2af7-bf59452870e5");
        
        /// <summary>Agent with repulsed expression on face</summary>
        public readonly static UUID EXPRESS_REPULSED = new UUID("d26fbcbc-1985-92ef-0cf0-7148b7ed0c08");
        
        /// <summary>Agent sticking tongue out</summary>
        public readonly static UUID EXPRESS_TONGUE_OUT = new UUID("8dbc8a59-18ec-c9c0-8edf-ddcb8a749e5f");
        
        /// <summary>Agent with big toothy smile</summary>
        public readonly static UUID EXPRESS_TOOTHSMILE = new UUID("d918a0d8-7291-ce83-762f-3f68dcc7402d");
        
        /// <summary>Agent winking</summary>
        public readonly static UUID EXPRESS_WINK = new UUID("1ac2c678-3ab4-410b-2aad-30a7dc9c4504");
        
        /// <summary>Agent wagging finger (disapproval)</summary>
        public readonly static UUID FINGER_WAG = new UUID("230ff836-fe1e-ab33-80c6-ef029c658136");
        
        /// <summary>Agent greeting another</summary>
        public readonly static UUID HELLO = new UUID("815c3d49-d7fa-0214-3053-8eb9e63fda4c");
        
        /// <summary>Agent point to lips then rear end</summary>
        public readonly static UUID KISS_MY_BUTT = new UUID("76f7ced2-4f91-31c8-ff7d-e76b19abfa40");
        
        /// <summary></summary>
        public readonly static UUID MUSCLE_BEACH = new UUID("52d9684f-2dcf-c252-2360-186b4236d07e");
        
        /// <summary>Agent moving head side to side</summary>
        public readonly static UUID NO = new UUID("0401dc30-0fdd-1fc3-3e51-c683fe117184");

        /// <summary>Agent moving head side to side with unhappy expression</summary>
        public readonly static UUID NO_UNHAPPY = new UUID("aca78492-0487-03ec-4eb5-5c8e76c837e1");

        /// <summary>Agent taunting another</summary>
        public readonly static UUID NYAH_NYAH = new UUID("a94ef3a0-7119-67b6-67e0-d93d5b08a544");
        
        /// <summary>Agent pointing at self</summary>
        public readonly static UUID POINT_ME = new UUID("c63ac707-6325-14c8-d681-54b81d0dff77");
        
        /// <summary>Agent pointing at another</summary>
        public readonly static UUID POINT_YOU = new UUID("97c5742d-1d74-9408-2bae-9738a9e9d084");

        /// <summary>Agent acting repulsed</summary>
        public readonly static UUID REPULSED = new UUID("d26fbcbc-1985-92ef-0cf0-7148b7ed0c08");
        
        /// <summary>Agent trying to be Chuck Norris</summary>
        public readonly static UUID ROUNDHOUSE_KICK = new UUID("49aea43b-5ac3-8a44-b595-96100af0beda");

        /// <summary>RPS count down</summary>
        public readonly static UUID RPS_COUNTDOWN = new UUID("8b753e16-bd7d-2bb3-7765-671b7b31e77c");

        /// <summary>Agent with hand flat over other hand</summary>
        public readonly static UUID RPS_PAPER = new UUID("a6a09cb1-0845-5ec0-051e-16bb79b30ddf");

        /// <summary>Agent with fist over other hand</summary>
        public readonly static UUID RPS_ROCK = new UUID("af2b8320-e579-2c41-b717-969759aeb3ca");

        /// <summary>Agent with two fingers spread over other hand</summary>
        public readonly static UUID RPS_SCISSORS = new UUID("8868cfa3-5b82-97ab-dc38-ded906f7fd90");

        /// <summary>Agent shrugging shoulders</summary>
        public readonly static UUID SHRUG = new UUID("e4485e5d-6bcc-e5ca-96fd-303227e93d6c");
        
        /// <summary>Agent inhaling smoke</summary>
        public readonly static UUID SMOKE_INHALE = new UUID("5a68100b-e6ba-5170-649c-80beddc67035");
        
        /// <summary>Agent thowing smoke</summary>
        public readonly static UUID SMOKE_THROW_DOWN = new UUID("0247dbfa-f37e-64ab-d09f-2678884c7f4a");
        
        /// <summary>Agent stretching - Yawn Female (not for robots)</summary>
        public readonly static UUID STRETCH = new UUID("865c5fe0-aafb-671a-8ad5-778beb24b6f8");
        
        /// <summary>Agent whispering with fingers in mouth</summary>
        public readonly static UUID WHISTLE = new UUID("b6f560d1-e2ad-24df-344c-2943c47a6775");
        
        /// <summary>Agent winking</summary>
        public readonly static UUID WINK = new UUID("1ac2c678-3ab4-410b-2aad-30a7dc9c4504");

        /// <summary>Agent winking</summary>
        public readonly static UUID WINK_HOLLYWOOD = new UUID("3f1cf9b6-f52c-46cd-bcf7-ffb7c8c25aea");

        /// <summary>Agent worried</summary>
        public readonly static UUID WORRY = new UUID("e7135c15-a7bc-637e-2664-b6fbdc22428c");
        
        /// <summary>Agent nodding yes</summary>
        public readonly static UUID YES = new UUID("43417d10-a2f7-7727-6fd4-dd7f04ad7a13");
        
        /// <summary>Agent nodding yes with happy face</summary>
        public readonly static UUID YES_HAPPY = new UUID("0eeb5ef7-10dd-4e4a-11a5-d053011fcdf2");
        
        /// <summary>Agent floating with legs and arms crossed</summary>
        public readonly static UUID YOGA_FLOAT = new UUID("42ecd00b-9947-a97c-400a-bbc9174c7aeb");

        /// <summary>Agent hula </summary>
        public readonly static UUID HULA = new UUID("4b30f435-499f-32fe-f2a0-7e7bce8d881d");

        /*
         * Not Implemented
         * 
        /// <summary>Agent on ground unanimated</summary>
        public readonly static UUID DEAD = new UUID("57abaae6-1d17-7b1b-5f98-6d11a6411276");
        /// <summary>Agent boozing it up</summary>
        public readonly static UUID DRINK = new UUID("0f86e355-dd31-a61c-fdb0-3a96b9aad05f");
        /// <summary>Agent with embarassed expression on face</summary>
        public readonly static UUID EMBARRASSED = new UUID("514af488-9051-044a-b3fc-d4dbf76377c6");
        /// <summary>Agent expressing sadness</summary>
        public readonly static UUID EXPRESS_SAD = new UUID("eb6ebfb2-a4b3-a19c-d388-4dd5c03823f7");
        /// <summary>Agent shrugging shoulders</summary>
        public readonly static UUID EXPRESS_SHRUG = new UUID("a351b1bc-cc94-aac2-7bea-a7e6ebad15ef");
        /// <summary>Agent with a smile</summary>
        public readonly static UUID EXPRESS_SMILE = new UUID("b7c7c833-e3d3-c4e3-9fc0-131237446312");
        /// <summary>Agent expressing surprise</summary>
        public readonly static UUID EXPRESS_SURPRISE = new UUID("728646d9-cc79-08b2-32d6-937f0a835c24");
        /// <summary>Agent expressing worry</summary>
        public readonly static UUID EXPRESS_WORRY = new UUID("e7135c15-a7bc-637e-2664-b6fbdc22428c");
        /// <summary>Agent falling down</summary>
        public readonly static UUID FALLDOWN = new UUID("666307d9-a860-572d-6fd4-c3ab8865c094");
        /// <summary>Agent walking (feminine version)</summary>
        public readonly static UUID FEMALE_WALK = new UUID("f5fc7433-043d-e819-8298-f519a119b688");
        /// <summary>I'm not sure I want to know</summary>
        public readonly static UUID FIST_PUMP = new UUID("7db00ccd-f380-f3ee-439d-61968ec69c8a");
        /// <summary>Agent in superman position</summary>
        public readonly static UUID FLY = new UUID("aec4610c-757f-bc4e-c092-c6e9caf18daf");
        /// <summary>Agent in superman position</summary>
        public readonly static UUID FLYSLOW = new UUID("2b5a38b2-5e00-3a97-a495-4c826bc443e6");
        /// <summary>Agent holding bazooka (right handed)</summary>
        public readonly static UUID HOLD_BAZOOKA_R = new UUID("ef62d355-c815-4816-2474-b1acc21094a6");
        /// <summary>Agent holding a bow (left handed)</summary>
        public readonly static UUID HOLD_BOW_L = new UUID("8b102617-bcba-037b-86c1-b76219f90c88");
        /// <summary>Agent holding a handgun (right handed)</summary>
        public readonly static UUID HOLD_HANDGUN_R = new UUID("efdc1727-8b8a-c800-4077-975fc27ee2f2");
        /// <summary>Agent holding a rifle (right handed)</summary>
        public readonly static UUID HOLD_RIFLE_R = new UUID("3d94bad0-c55b-7dcc-8763-033c59405d33");
        /// <summary>Agent throwing an object (right handed)</summary>
        public readonly static UUID HOLD_THROW_R = new UUID("7570c7b5-1f22-56dd-56ef-a9168241bbb6");
        /// <summary>Agent in static hover</summary>
        public readonly static UUID HOVER = new UUID("4ae8016b-31b9-03bb-c401-b1ea941db41d");
        /// <summary>Agent hovering downward</summary>
        public readonly static UUID HOVER_DOWN = new UUID("20f063ea-8306-2562-0b07-5c853b37b31e");
        /// <summary>Agent hovering upward</summary>
        public readonly static UUID HOVER_UP = new UUID("62c5de58-cb33-5743-3d07-9e4cd4352864");
        /// <summary>Agent being impatient</summary>
        public readonly static UUID IMPATIENT = new UUID("5ea3991f-c293-392e-6860-91dfa01278a3");
        /// <summary>Agent jumping</summary>
        public readonly static UUID JUMP = new UUID("2305bd75-1ca9-b03b-1faa-b176b8a8c49e");
        /// <summary>Agent jumping with fervor</summary>
        public readonly static UUID JUMP_FOR_JOY = new UUID("709ea28e-1573-c023-8bf8-520c8bc637fa");
        /// <summary>Agent landing from jump, finished flight, etc</summary>
        public readonly static UUID LAND = new UUID("7a17b059-12b2-41b1-570a-186368b6aa6f");
        /// <summary>Agent laughing</summary>
        public readonly static UUID LAUGH_SHORT = new UUID("ca5b3f14-3194-7a2b-c894-aa699b718d1f");
        /// <summary>Agent landing from jump, finished flight, etc</summary>
        public readonly static UUID MEDIUM_LAND = new UUID("f4f00d6e-b9fe-9292-f4cb-0ae06ea58d57");
        /// <summary>Agent sitting on a motorcycle</summary>
        public readonly static UUID MOTORCYCLE_SIT = new UUID("08464f78-3a8e-2944-cba5-0c94aff3af29");
        /// <summary></summary>
        public readonly static UUID ONETWO_PUNCH = new UUID("eefc79be-daae-a239-8c04-890f5d23654a");
        /// <summary>Agent giving peace sign</summary>
        public readonly static UUID PEACE = new UUID("b312b10e-65ab-a0a4-8b3c-1326ea8e3ed9");
        /// <summary>Agent preparing for jump (bending knees)</summary>
        public readonly static UUID PRE_JUMP = new UUID("7a4e87fe-de39-6fcb-6223-024b00893244");
        /// <summary>Agent punching with left hand</summary>
        public readonly static UUID PUNCH_LEFT = new UUID("f3300ad9-3462-1d07-2044-0fef80062da0");
        /// <summary>Agent punching with right hand</summary>
        public readonly static UUID PUNCH_RIGHT = new UUID("c8e42d32-7310-6906-c903-cab5d4a34656");
        /// <summary>Agent running</summary>
        public readonly static UUID RUN = new UUID("05ddbff8-aaa9-92a1-2b74-8fe77a29b445");
        /// <summary>Agent appearing sad</summary>
        public readonly static UUID SAD = new UUID("0eb702e2-cc5a-9a88-56a5-661a55c0676a");
        /// <summary>Agent saluting</summary>
        public readonly static UUID SALUTE = new UUID("cd7668a6-7011-d7e2-ead8-fc69eff1a104");
        /// <summary>Agent shooting bow (left handed)</summary>
        public readonly static UUID SHOOT_BOW_L = new UUID("e04d450d-fdb5-0432-fd68-818aaf5935f8");
        /// <summary>Agent cupping mouth as if shouting</summary>
        public readonly static UUID SHOUT = new UUID("6bd01860-4ebd-127a-bb3d-d1427e8e0c42");
        /// <summary>Agent in sit position</summary>
        public readonly static UUID SIT = new UUID("1a5fe8ac-a804-8a5d-7cbd-56bd83184568");
        /// <summary>Agent in sit position (feminine)</summary>
        public readonly static UUID SIT_FEMALE = new UUID("b1709c8d-ecd3-54a1-4f28-d55ac0840782");
        /// <summary>Agent in sit position (generic)</summary>
        public readonly static UUID SIT_GENERIC = new UUID("245f3c54-f1c0-bf2e-811f-46d8eeb386e7");
        /// <summary>Agent sitting on ground</summary>
        public readonly static UUID SIT_GROUND = new UUID("1c7600d6-661f-b87b-efe2-d7421eb93c86");
        /// <summary>Agent sitting on ground</summary>
        public readonly static UUID SIT_GROUND_staticRAINED = new UUID("1a2bd58e-87ff-0df8-0b4c-53e047b0bb6e");
        /// <summary></summary>
        public readonly static UUID SIT_TO_STAND = new UUID("a8dee56f-2eae-9e7a-05a2-6fb92b97e21e");
        /// <summary>Agent sleeping on side</summary>
        public readonly static UUID SLEEP = new UUID("f2bed5f9-9d44-39af-b0cd-257b2a17fe40");
        /// <summary>Agent smoking</summary>
        public readonly static UUID SMOKE_IDLE = new UUID("d2f2ee58-8ad1-06c9-d8d3-3827ba31567a");
        /// <summary>Agent taking a picture</summary>
        public readonly static UUID SNAPSHOT = new UUID("eae8905b-271a-99e2-4c0e-31106afd100c");
        /// <summary>Agent standing</summary>
        public readonly static UUID STAND = new UUID("2408fe9e-df1d-1d7d-f4ff-1384fa7b350f");
        /// <summary>Agent standing up</summary>
        public readonly static UUID STANDUP = new UUID("3da1d753-028a-5446-24f3-9c9b856d9422");
        /// <summary>Agent standing</summary>
        public readonly static UUID STAND_1 = new UUID("15468e00-3400-bb66-cecc-646d7c14458e");
        /// <summary>Agent standing</summary>
        public readonly static UUID STAND_2 = new UUID("370f3a20-6ca6-9971-848c-9a01bc42ae3c");
        /// <summary>Agent standing</summary>
        public readonly static UUID STAND_3 = new UUID("42b46214-4b44-79ae-deb8-0df61424ff4b");
        /// <summary>Agent standing</summary>
        public readonly static UUID STAND_4 = new UUID("f22fed8b-a5ed-2c93-64d5-bdd8b93c889f");
        /// <summary>Agent in stride (fast walk)</summary>
        public readonly static UUID STRIDE = new UUID("1cb562b0-ba21-2202-efb3-30f82cdf9595");
        /// <summary>Agent surfing</summary>
        public readonly static UUID SURF = new UUID("41426836-7437-7e89-025d-0aa4d10f1d69");
        /// <summary>Agent acting surprised</summary>
        public readonly static UUID SURPRISE = new UUID("313b9881-4302-73c0-c7d0-0e7a36b6c224");
        /// <summary>Agent striking with a sword</summary>
        public readonly static UUID SWORD_STRIKE = new UUID("85428680-6bf9-3e64-b489-6f81087c24bd");
        /// <summary>Agent talking (lips moving)</summary>
        public readonly static UUID TALK = new UUID("5c682a95-6da4-a463-0bf6-0f5b7be129d1");
        /// <summary>Agent throwing a tantrum</summary>
        public readonly static UUID TANTRUM = new UUID("11000694-3f41-adc2-606b-eee1d66f3724");
        /// <summary>Agent throwing an object (right handed)</summary>
        public readonly static UUID THROW_R = new UUID("aa134404-7dac-7aca-2cba-435f9db875ca");
        /// <summary>Agent trying on a shirt</summary>
        public readonly static UUID TRYON_SHIRT = new UUID("83ff59fe-2346-f236-9009-4e3608af64c1");
        /// <summary>Agent turning to the left</summary>
        public readonly static UUID TURNLEFT = new UUID("56e0ba0d-4a9f-7f27-6117-32f2ebbf6135");
        /// <summary>Agent turning to the right</summary>
        public readonly static UUID TURNRIGHT = new UUID("2d6daa51-3192-6794-8e2e-a15f8338ec30");
        /// <summary>Agent typing</summary>
        public readonly static UUID TYPE = new UUID("c541c47f-e0c0-058b-ad1a-d6ae3a4584d9");
        /// <summary>Agent walking</summary>
        public readonly static UUID WALK = new UUID("6ed24bd8-91aa-4b12-ccc7-c97c857ab4e0");
        /// <summary>Agent whispering</summary>
        public readonly static UUID WHISPER = new UUID("7693f268-06c7-ea71-fa21-2b30d6533f8f");
        /// <summary>Agent doing a curtsey bow</summary>
        public readonly static UUID COURTBOW = new UUID("9ba1c942-08be-e43a-fb29-16ad440efc50");
        /// <summary>Agent crouching</summary>
        public readonly static UUID CROUCH = new UUID("201f3fdf-cb1f-dbec-201f-7333e328ae7c");
        /// <summary>Agent crouching while walking</summary>
        public readonly static UUID CROUCHWALK = new UUID("47f5f6fb-22e5-ae44-f871-73aaaf4a6022");
        /// <summary>Agent unanimated with arms out (e.g. setting appearance)</summary>
        public readonly static UUID CUSTOMIZE = new UUID("038fcec9-5ebd-8a8e-0e2e-6e71a0a1ac53");
        /// <summary>Agent re-animated after set appearance finished</summary>
        public readonly static UUID CUSTOMIZE_DONE = new UUID("6883a61a-b27b-5914-a61e-dda118a9ee2c");
        /// <summary>Agent aiming a bazooka (right handed)</summary>
        public readonly static UUID AIM_BAZOOKA_R = new UUID("b5b4a67d-0aee-30d2-72cd-77b333e932ef");
        /// <summary>Agent aiming a bow (left handed)</summary>
        public readonly static UUID AIM_BOW_L = new UUID("46bb4359-de38-4ed8-6a22-f1f52fe8f506");
        /// <summary>Agent aiming a hand gun (right handed)</summary>
        public readonly static UUID AIM_HANDGUN_R = new UUID("3147d815-6338-b932-f011-16b56d9ac18b");
        /// <summary>Agent aiming a rifle (right handed)</summary>
        public readonly static UUID AIM_RIFLE_R = new UUID("ea633413-8006-180a-c3ba-96dd1d756720");
        */
    }
}
