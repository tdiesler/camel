(function(){
var j=null,n=true;window.PR_SHOULD_USE_CONTINUATION=n;window.PR_TAB_WIDTH=8;window.PR_normalizedHtml=window.PR=window.prettyPrintOne=window.prettyPrint=void 0;window._pr_isIE6=function(){var K=navigator&&navigator.userAgent&&/\bMSIE 6\./.test(navigator.userAgent);window._pr_isIE6=function(){return K};return K};
var ba="a",ca="z",da="A",ea="Z",fa="!",ga="!=",ha="!==",ia="#",ja="%",ka="%=",x="&",la="&&",ma="&&=",na="&=",oa="(",pa="*",qa="*=",ra="+=",sa=",",ta="-=",ua="->",va="/",Da="/=",Ea=":",Fa="::",Ga=";",z="<",Ha="<<",Ia="<<=",Ja="<=",Ka="=",La="==",Ma="===",B=">",Na=">=",Oa=">>",Pa=">>=",Qa=">>>",Ra=">>>=",Sa="?",Ta="@",Ua="[",Va="^",Wa="^=",Xa="^^",Ya="^^=",Za="{",$a="|",ab="|=",bb="||",cb="||=",db="~",eb="break",fb="case",gb="continue",hb="delete",ib="do",jb="else",kb="finally",lb="instanceof",mb="return",
nb="throw",ob="try",pb="typeof",qb="(?:(?:(?:^|[^0-9.])\\.{1,3})|(?:(?:^|[^\\+])\\+)|(?:(?:^|[^\\-])-)",rb="|\\b",sb="\\$1",tb="|^)\\s*$",ub="&amp;",vb="&lt;",wb="&gt;",xb="&quot;",yb="&#",zb="x",Ab="'",C='"',Bb=" ",Cb="XMP",Db="</",Eb='="',D="PRE",Fb='<!DOCTYPE foo PUBLIC "foo bar">\n<foo />',H="",Gb="\t",Hb="\n",Ib="nocode",Jb=' $1="$2$3$4"',I="pln",L="lang-",M="src",N="default-markup",O="default-code",P="com",Kb="dec",S="pun",Lb="lang-js",Mb="lang-css",T="tag",U="atv",Nb="<>/=",V="atn",Ob=" \t\r\n",
W="str",Pb="'\"",Qb="'\"`",Rb="\"'",Sb=" \r\n",X="lit",Tb="123456789",Ub=".",Vb="kwd",Wb="typ",Xb="break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try alignof align_union asm axiom bool concept concept_map const_cast constexpr decltype dynamic_cast explicit export friend inline late_check mutable namespace nullptr reinterpret_cast static_assert static_cast template typeid typename typeof using virtual wchar_t where break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try boolean byte extends final finally implements import instanceof null native package strictfp super synchronized throws transient as base by checked decimal delegate descending event fixed foreach from group implicit in interface internal into is lock object out override orderby params readonly ref sbyte sealed stackalloc string select uint ulong unchecked unsafe ushort var break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try debugger eval export function get null set undefined var with Infinity NaN caller delete die do dump elsif eval exit foreach for goto if import last local my next no our print package redo require sub undef unless until use wantarray while BEGIN END break continue do else for if return while and as assert class def del elif except exec finally from global import in is lambda nonlocal not or pass print raise try with yield False True None break continue do else for if return while alias and begin case class def defined elsif end ensure false in module next nil not or redo rescue retry self super then true undef unless until when yield BEGIN END break continue do else for if return while case done elif esac eval fi function in local set then until ",
Y="</span>",Yb='<span class="',Zb='">',$b="$1&nbsp;",ac="<br />",bc="console",cc="cannot override language handler %s",dc="htm",ec="html",fc="mxml",gc="xhtml",hc="xml",ic="xsl",jc="break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try alignof align_union asm axiom bool concept concept_map const_cast constexpr decltype dynamic_cast explicit export friend inline late_check mutable namespace nullptr reinterpret_cast static_assert static_cast template typeid typename typeof using virtual wchar_t where ",
kc="c",lc="cc",mc="cpp",nc="cxx",oc="cyc",pc="m",qc="break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try boolean byte extends final finally implements import instanceof null native package strictfp super synchronized throws transient as base by checked decimal delegate descending event fixed foreach from group implicit in interface internal into is lock object out override orderby params readonly ref sbyte sealed stackalloc string select uint ulong unchecked unsafe ushort var ",
rc="cs",sc="break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try boolean byte extends final finally implements import instanceof null native package strictfp super synchronized throws transient ",tc="java",uc="break continue do else for if return while case done elif esac eval fi function in local set then until ",
vc="bsh",wc="csh",xc="sh",yc="break continue do else for if return while and as assert class def del elif except exec finally from global import in is lambda nonlocal not or pass print raise try with yield False True None ",zc="cv",Ac="py",Bc="caller delete die do dump elsif eval exit foreach for goto if import last local my next no our print package redo require sub undef unless until use wantarray while BEGIN END ",Cc="perl",Dc="pl",Ec="pm",Fc="break continue do else for if return while alias and begin case class def defined elsif end ensure false in module next nil not or redo rescue retry self super then true undef unless until when yield BEGIN END ",
Gc="rb",Hc="break continue do else for if return while auto case char const default double enum extern float goto int long register short signed sizeof static struct switch typedef union unsigned void volatile catch class delete false import new operator private protected public this throw true try debugger eval export function get null set undefined var with Infinity NaN ",Ic="js",Jc="pre",Kc="code",Lc="xmp",Mc="prettyprint",Nc="class",Oc="br",Pc="\r\n";
(function(){function K(b){b=b.split(/ /g);var a={};for(var d=b.length;--d>=0;){var c=b[d];if(c)a[c]=j}return a}function Qc(b){return b>=ba&&b<=ca||b>=da&&b<=ea}function Q(b,a,d,c){b.unshift(d,c||0);try{a.splice.apply(a,b)}finally{b.splice(0,2)}}var Rc=(function(){var b=[fa,ga,ha,ia,ja,ka,x,la,ma,na,oa,pa,qa,ra,sa,ta,ua,va,Da,Ea,Fa,Ga,z,Ha,Ia,Ja,Ka,La,Ma,B,Na,Oa,Pa,Qa,Ra,Sa,Ta,Ua,Va,Wa,Xa,Ya,Za,$a,ab,bb,cb,db,eb,fb,gb,hb,ib,jb,kb,lb,mb,nb,ob,pb],a=qb;for(var d=0;d<b.length;++d){var c=b[d];a+=Qc(c.charAt(0))?
rb+c:$a+c.replace(/([^=<>:&])/g,sb)}a+=tb;return new RegExp(a)})(),wa=/&/g,xa=/</g,ya=/>/g,Sc=/\"/g;function Tc(b){return b.replace(wa,ub).replace(xa,vb).replace(ya,wb).replace(Sc,xb)}function Z(b){return b.replace(wa,ub).replace(xa,vb).replace(ya,wb)}var Uc=/&lt;/g,Vc=/&gt;/g,Wc=/&apos;/g,Xc=/&quot;/g,Yc=/&amp;/g,Zc=/&nbsp;/g;function $c(b){var a=b.indexOf(x);if(a<0)return b;for(--a;(a=b.indexOf(yb,a+1))>=0;){var d=b.indexOf(Ga,a);if(d>=0){var c=b.substring(a+3,d),g=10;if(c&&c.charAt(0)===zb){c=
c.substring(1);g=16}var e=parseInt(c,g);isNaN(e)||(b=b.substring(0,a)+String.fromCharCode(e)+b.substring(d+1))}}return b.replace(Uc,z).replace(Vc,B).replace(Wc,Ab).replace(Xc,C).replace(Yc,x).replace(Zc,Bb)}function za(b){return Cb===b.tagName}function R(b,a){switch(b.nodeType){case 1:var d=b.tagName.toLowerCase();a.push(z,d);for(var c=0;c<b.attributes.length;++c){var g=b.attributes[c];if(!!g.specified){a.push(Bb);R(g,a)}}a.push(B);for(var e=b.firstChild;e;e=e.nextSibling)R(e,a);if(b.firstChild||
!/^(?:br|link|img)$/.test(d))a.push(Db,d,B);break;case 2:a.push(b.name.toLowerCase(),Eb,Tc(b.value),C);break;case 3:case 4:a.push(Z(b.nodeValue));break}}var $=j;function ad(b){if(j===$){var a=document.createElement(D);a.appendChild(document.createTextNode(Fb));$=!/</.test(a.innerHTML)}if($){var d=b.innerHTML;if(za(b))d=Z(d);return d}var c=[];for(var g=b.firstChild;g;g=g.nextSibling)R(g,c);return c.join(H)}function bd(b){var a=0;return function(d){var c=j,g=0;for(var e=0,k=d.length;e<k;++e){var f=
d.charAt(e);switch(f){case Gb:c||(c=[]);c.push(d.substring(g,e));var h=b-a%b;a+=h;for(;h>=0;h-="                ".length)c.push("                ".substring(0,h));g=e+1;break;case Hb:a=0;break;default:++a}}if(!c)return d;c.push(d.substring(g));return c.join(H)}}var cd=/(?:[^<]+|<!--[\s\S]*?--\>|<!\[CDATA\[([\s\S]*?)\]\]>|<\/?[a-zA-Z][^>]*>|<)/g,dd=/^<!--/,ed=/^<\[CDATA\[/,fd=/^<br\b/i,Aa=/^<(\/?)([a-zA-Z]+)/;function gd(b){var a=b.match(cd),d=[],c=0,g=[];if(a)for(var e=0,k=a.length;e<k;++e){var f=
a[e];if(f.length>1&&f.charAt(0)===z){if(!dd.test(f))if(ed.test(f)){d.push(f.substring(9,f.length-3));c+=f.length-12}else if(fd.test(f)){d.push(Hb);++c}else if(f.indexOf(Ib)>=0&&hd(f)){var h=f.match(Aa)[2],q=1,i;a:for(i=e+1;i<k;++i){var o=a[i].match(Aa);if(o&&o[2]===h)if(o[1]===va){if(--q===0)break a}else++q}if(i<k){g.push(c,a.slice(e,i+1).join(H));e=i}else g.push(c,f)}else g.push(c,f)}else{var r=$c(f);d.push(r);c+=r.length}}return{source:d.join(H),tags:g}}function hd(b){return!!b.replace(/\s(\w+)\s*=\s*(?:\"([^\"]*)\"|'([^\']*)'|(\S+))/g,
Jb).match(/[cC][lL][aA][sS][sS]=\"[^\"]*\bnocode\b/)}function aa(b,a,d,c){if(!!a){var g=d.call({},a);if(b)for(var e=g.length;(e-=2)>=0;)g[e]+=b;c.push.apply(c,g)}}function J(b,a){var d={};(function(){var k=b.concat(a);for(var f=k.length;--f>=0;){var h=k[f],q=h[3];if(q)for(var i=q.length;--i>=0;)d[q.charAt(i)]=h}})();var c=a.length,g=/\S/,e=function(k,f){f=f||0;var h=[f,I],q=H,i=0,o=k;while(o.length){var r,l=j,m,p=d[o.charAt(0)];if(p){m=o.match(p[1]);l=m[0];r=p[0]}else{for(var s=0;s<c;++s){p=a[s];
var u=p[2];if(!(u&&!u.test(q))){if(m=o.match(p[1])){l=m[0];r=p[0];break}}}if(!l){r=I;l=o.substring(0,1)}}var t=L===r.substring(0,5);if(t&&!(m&&m[1])){t=false;r=M}if(t){var A=m[1],v=l.indexOf(A),E=v+A.length,F=r.substring(5);G.hasOwnProperty(F)||(F=/^\s*</.test(A)?N:O);aa(f+i,l.substring(0,v),e,h);aa(f+i+v,l.substring(v,E),G[F],h);aa(f+i+E,l.substring(E),e,h)}else h.push(f+i,r);i+=l.length;o=o.substring(l.length);if(r!==P&&g.test(l))q=l}return h};return e}var id=J([],[[I,/^[^<?]+/,j],[Kb,/^<!\w[^>]*(?:>|$)/,
j],[P,/^<!--[\s\S]*?(?:--\>|$)/,j],[L,/^<\?([\s\S]+?)(?:\?>|$)/,j],[L,/^<%([\s\S]+?)(?:%>|$)/,j],[S,/^(?:<[%?]|[%?]>)/,j],[L,/^<xmp\b[^>]*>([\s\S]+?)<\/xmp\b[^>]*>/i,j],[Lb,/^<script\b[^>]*>([\s\S]+?)<\/script\b[^>]*>/i,j],[Mb,/^<style\b[^>]*>([\s\S]+?)<\/style\b[^>]*>/i,j],[T,/^<\/?\w[^<>]*>/,j]]),jd=/^(<[^>]*>)([\s\S]*)(<\/[^>]*>)$/;function kd(b){var a=id(b);for(var d=0;d<a.length;d+=2)if(a[d+1]===M){var c,g;c=a[d];g=d+2<a.length?a[d+2]:b.length;var e=b.substring(c,g),k=e.match(jd);if(k)a.splice(d,
2,c,T,c+k[1].length,M,c+k[1].length+(k[2]||H).length,T)}return a}var ld=J([[U,/^\'[^\']*(?:\'|$)/,j,Ab],[U,/^\"[^\"]*(?:\"|$)/,j,C],[S,/^[<>\/=]+/,j,Nb]],[[T,/^[\w:\-]+/,/^</],[U,/^[\w\-]+/,/^=/],[V,/^[\w:\-]+/,j],[I,/^\s+/,j,Ob]]);function md(b,a){for(var d=0;d<a.length;d+=2){var c=a[d+1];if(c===T){var g,e;g=a[d];e=d+2<a.length?a[d+2]:b.length;var k=b.substring(g,e),f=ld(k,g);Q(f,a,d,2);d+=f.length-2}}return a}function y(b){var a=[],d=[];if(b.tripleQuotedStrings)a.push([W,/^(?:\'\'\'(?:[^\'\\]|\\[\s\S]|\'{1,2}(?=[^\']))*(?:\'\'\'|$)|\"\"\"(?:[^\"\\]|\\[\s\S]|\"{1,2}(?=[^\"]))*(?:\"\"\"|$)|\'(?:[^\\\']|\\[\s\S])*(?:\'|$)|\"(?:[^\\\"]|\\[\s\S])*(?:\"|$))/,
j,Pb]);else b.multiLineStrings?a.push([W,/^(?:\'(?:[^\\\']|\\[\s\S])*(?:\'|$)|\"(?:[^\\\"]|\\[\s\S])*(?:\"|$)|\`(?:[^\\\`]|\\[\s\S])*(?:\`|$))/,j,Qb]):a.push([W,/^(?:\'(?:[^\\\'\r\n]|\\.)*(?:\'|$)|\"(?:[^\\\"\r\n]|\\.)*(?:\"|$))/,j,Rb]);d.push([I,/^(?:[^\'\"\`\/\#]+)/,j,Sb]);b.hashComments&&a.push([P,/^#[^\r\n]*/,j,ia]);if(b.cStyleComments){d.push([P,/^\/\/[^\r\n]*/,j]);d.push([P,/^\/\*[\s\S]*?(?:\*\/|$)/,j])}b.regexLiterals&&d.push([W,/^\/(?=[^\/*])(?:[^\/\x5B\x5C]|\x5C[\s\S]|\x5B(?:[^\x5C\x5D]|\x5C[\s\S])*(?:\x5D|$))+(?:\/|$)/,
Rc]);var c=K(b.keywords),g=J(a,d),e=J([],[[I,/^\s+/,j,Sb],[I,/^[a-z_$@][a-z_$@0-9]*/i,j],[X,/^0x[a-f0-9]+[a-z]/i,j],[X,/^(?:\d(?:_\d+)*\d*(?:\.\d*)?|\.\d+)(?:e[+\-]?\d+)?[a-z]*/i,j,Tb],[S,/^[^\s\w\.$@]+/,j]]);function k(f,h){for(var q=0;q<h.length;q+=2){var i=h[q+1];if(i===I){var o,r,l,m;o=h[q];r=q+2<h.length?h[q+2]:f.length;l=f.substring(o,r);m=e(l,o);for(var p=0,s=m.length;p<s;p+=2){var u=m[p+1];if(u===I){var t=m[p],A=p+2<s?m[p+2]:l.length,v=f.substring(t,A);if(v===Ub)m[p+1]=S;else if(v in c)m[p+
1]=Vb;else if(/^@?[A-Z][A-Z$]*[a-z][A-Za-z$]*$/.test(v))m[p+1]=v.charAt(0)===Ta?X:Wb}}Q(m,h,q,2);q+=m.length-2}}return h}return function(f){var h=g(f);return h=k(f,h)}}var Ba=y({keywords:Xb,hashComments:n,cStyleComments:n,multiLineStrings:n,regexLiterals:n});function nd(b,a){var d=false;for(var c=0;c<a.length;c+=2){var g=a[c+1],e,k;if(g===V){e=a[c];k=c+2<a.length?a[c+2]:b.length;d=/^on|^style$/i.test(b.substring(e,k))}else if(g===U){if(d){e=a[c];k=c+2<a.length?a[c+2]:b.length;var f=b.substring(e,
k),h=f.length,q=h>=2&&/^[\"\']/.test(f)&&f.charAt(0)===f.charAt(h-1),i,o,r;if(q){o=e+1;r=k-1;i=f}else{o=e+1;r=k-1;i=f.substring(1,f.length-1)}var l=Ba(i);for(var m=0,p=l.length;m<p;m+=2)l[m]+=o;if(q){l.push(r,U);Q(l,a,c+2,0)}else Q(l,a,c,2)}d=false}}return a}function od(b){var a=kd(b);a=md(b,a);return a=nd(b,a)}function pd(b,a,d){var c=[],g=0,e=j,k=j,f=0,h=0,q=bd(window.PR_TAB_WIDTH),i=/([\r\n ]) /g,o=/(^| ) /gm,r=/\r\n?|\n/g,l=/[ \r\n]$/,m=n;function p(u){if(u>g){if(e&&e!==k){c.push(Y);e=j}if(!e&&
k){e=k;c.push(Yb,e,Zb)}var t=Z(q(b.substring(g,u))).replace(m?o:i,$b);m=l.test(t);c.push(t.replace(r,ac));g=u}}while(n){var s;if(s=f<a.length?h<d.length?a[f]<=d[h]:n:false){p(a[f]);if(e){c.push(Y);e=j}c.push(a[f+1]);f+=2}else if(h<d.length){p(d[h]);k=d[h+1];h+=2}else break}p(b.length);e&&c.push(Y);return c.join(H)}var G={};function w(b,a){for(var d=a.length;--d>=0;){var c=a[d];if(G.hasOwnProperty(c))bc in window&&console.log(cc,c);else G[c]=b}}w(Ba,[O]);w(od,[N,dc,ec,fc,gc,hc,ic]);w(y({keywords:jc,
hashComments:n,cStyleComments:n}),[kc,lc,mc,nc,oc,pc]);w(y({keywords:qc,hashComments:n,cStyleComments:n}),[rc]);w(y({keywords:sc,cStyleComments:n}),[tc]);w(y({keywords:uc,hashComments:n,multiLineStrings:n}),[vc,wc,xc]);w(y({keywords:yc,hashComments:n,multiLineStrings:n,tripleQuotedStrings:n}),[zc,Ac]);w(y({keywords:Bc,hashComments:n,multiLineStrings:n,regexLiterals:n}),[Cc,Dc,Ec]);w(y({keywords:Fc,hashComments:n,multiLineStrings:n,regexLiterals:n}),[Gc]);w(y({keywords:Hc,cStyleComments:n,regexLiterals:n}),
[Ic]);function Ca(b,a){try{var d=gd(b),c=d.source,g=d.tags;G.hasOwnProperty(a)||(a=/^\s*</.test(c)?N:O);var e=G[a].call({},c);return pd(c,g,e)}catch(k){if(bc in window){console.log(k);console.a()}return b}}function qd(b){var a=window._pr_isIE6(),d=[document.getElementsByTagName(Jc),document.getElementsByTagName(Kc),document.getElementsByTagName(Lc)],c=[];for(var g=0;g<d.length;++g)for(var e=0,k=d[g].length;e<k;++e)c.push(d[g][e]);var f=0;function h(){var q=window.PR_SHOULD_USE_CONTINUATION?(new Date).getTime()+
250:Infinity;for(;f<c.length&&(new Date).getTime()<q;f++){var i=c[f];if(i.className&&i.className.indexOf(Mc)>=0){var o=i.className.match(/\blang-(\w+)\b/);if(o)o=o[1];var r=false;for(var l=i.parentNode;l;l=l.parentNode)if((l.tagName===Jc||l.tagName===Kc||l.tagName===Lc)&&l.className&&l.className.indexOf(Mc)>=0){r=n;break}if(!r){var m=ad(i);m=m.replace(/(?:\r\n?|\n)$/,H);var p=Ca(m,o);if(za(i)){var s=document.createElement(D);for(var u=0;u<i.attributes.length;++u){var t=i.attributes[u];if(t.specified){var A=
t.name.toLowerCase();if(A===Nc)s.className=t.value;else s.setAttribute(t.name,t.value)}}s.innerHTML=p;i.parentNode.replaceChild(s,i);i=s}else i.innerHTML=p;if(a&&i.tagName===D){var v=i.getElementsByTagName(Oc);for(var E=v.length;--E>=0;){var F=v[E];F.parentNode.replaceChild(document.createTextNode(Pc),F)}}}}}if(f<c.length)setTimeout(h,250);else b&&b()}h()}window.PR_normalizedHtml=R;window.prettyPrintOne=Ca;window.prettyPrint=qd;window.PR={createSimpleLexer:J,registerLangHandler:w,sourceDecorator:y,
PR_ATTRIB_NAME:V,PR_ATTRIB_VALUE:U,PR_COMMENT:P,PR_DECLARATION:Kb,PR_KEYWORD:Vb,PR_LITERAL:X,PR_NOCODE:Ib,PR_PLAIN:I,PR_PUNCTUATION:S,PR_SOURCE:M,PR_STRING:W,PR_TAG:T,PR_TYPE:Wb}})();
})()
