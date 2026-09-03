/**
 * 建築物電信設備及空間設置使用管理規則 — 導覽資料
 * pcode: K0060040
 */
const LAW = {
  pcode: 'K0060040',
  name: '建築物電信設備及空間設置使用管理規則',
  shortName: '屋內外電信設備規則',
  amended: '民國 104 年 08 月 05 日',
  fullUrl: 'https://law.moj.gov.tw/LawClass/LawAll.aspx?pcode=K0060040',
  updated: '2026/09/03',
  author: 'AJ',
};

function mojArticleUrl(article) {
  const flno = String(article).replace(/\s+/g, '');
  return `https://law.moj.gov.tw/LawClass/LawSingle.aspx?pcode=${LAW.pcode}&flno=${encodeURIComponent(flno)}`;
}

const ATTACHMENTS = [
  { label: '附圖一', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234795&lan=C' },
  { label: '附圖二', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234796&lan=C' },
  { label: '附圖三', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234797&lan=C' },
  { label: '附圖四', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234798&lan=C' },
  { label: '附件一：電信室面積一覽表', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234799&lan=C' },
  { label: '附件二：洽辦／審查／審驗申請表', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234800&lan=C' },
  { label: '附件三：洽辦／審查／審驗作業流程圖', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234801&lan=C' },
  { label: '附件四：電信設備審定證明', url: 'https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234802&lan=C' },
];

const CATEGORIES = [
  {
    id: 'general',
    icon: '📋',
    title: '通則',
    subtitle: '第 1–3 條',
    desc: '立法依據、適用範圍及用詞定義',
    articles: [
      { art: '1', label: '本規則依電信法第三十八條第六項規定訂定之。' },
      { art: '2', label: '建築物屋內外電信設備及其空間之設置及使用，應依本規則之規定…' },
      { art: '3', label: '本規則用詞定義如下：' },
    ],
  },
  {
    id: 'duty',
    icon: '📡',
    title: '設置義務與責任分界',
    subtitle: '第 4–7 條',
    desc: '屋內外電信設備設置義務、責任分界點及維護責任',
    articles: [
      { art: '4', label: '建築物建造時，起造人應依規定設置屋內外電信設備…' },
      { art: '5', label: '建築物電信設備連接市內網路業務經營者之電信網路設備…' },
      { art: '6', label: '前條之設置及維護責任分界規定如下：', note: '含附圖一～四' },
      { art: '7', label: '建築物責任分界點以外之公眾電信固定通信網路設施…' },
    ],
  },
  {
    id: 'equipment',
    icon: '🏢',
    title: '電信設備與電信室',
    subtitle: '第 8–9 條',
    desc: '應設置之電信設備、光纜引進及電信室設置條件',
    articles: [
      { art: '8', label: '起造人或所有人應設置下列建築物電信設備及其空間：' },
      { art: '9', label: '新建建築物為收容市內網路業務經營者之電信設備…', note: '含附件一' },
    ],
  },
  {
    id: 'design',
    icon: '📐',
    title: '設計與檢測',
    subtitle: '第 10 條',
    desc: '工程技術規範、設計簽證及屋外管線',
    articles: [
      { art: '10', label: '建築物屋內外電信設備及相關設置空間之設計…' },
    ],
  },
  {
    id: 'review',
    icon: '📝',
    title: '洽辦審查審驗',
    subtitle: '第 11–13 條',
    desc: '洽辦、設計審查、竣工審驗及電信服務申請',
    articles: [
      { art: '11', label: '建築物起造人於設計屋內外電信設備及其空間時…', note: '含附件二' },
      { art: '12', label: '建築物電信設備設置完成後，建築物起造人應檢具下列文件…', note: '含附件三、四' },
      { art: '13', label: '建築物電信設備及其空間經審驗合格，市內網路業務經營者…' },
    ],
  },
  {
    id: 'operation',
    icon: '⚙️',
    title: '使用維護與管理',
    subtitle: '第 14–21 條',
    desc: '資料保存、施工維護、集線室及竣工圖移交',
    articles: [
      { art: '14', label: '市內網路業務經營者應保存完成洽辦之申請表…' },
      { art: '15', label: '市內網路業務經營者或其他第三人受託代建築物起造人…' },
      { art: '16', label: '建築物所有人所設置之電信設備不符本規則之規定…' },
      { art: '17', label: '連接第一類電信事業之建築物責任分界點以內之所有電信設備…' },
      { art: '18', label: '建築物內部自用電信機械設備，如用戶專用交換機等…' },
      { art: '19', label: '市內網路業務經營者利用設置於電信室之電信設備…' },
      { art: '20', label: '市內網路業務經營者為建設其電信網路之需要…' },
      { art: '21', label: '建築物電信管箱、配線等電信設備設置…' },
    ],
  },
  {
    id: 'supplement',
    icon: '📎',
    title: '附則',
    subtitle: '第 22 條',
    desc: '施行日期及過渡規定',
    articles: [
      { art: '22', label: '本規則自發布日施行。' },
    ],
  },
  {
    id: 'attachments',
    icon: '📂',
    title: '附圖與附件',
    subtitle: 'MOJ 官方 PDF',
    desc: '第 6、9、11、12 條引用之附圖及附件',
    files: true,
  },
];
